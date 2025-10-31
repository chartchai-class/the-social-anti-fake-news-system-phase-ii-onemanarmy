import { defineStore } from 'pinia'
import apiClient from '@/services/AxiosClient'

export type Role = 'ROLE_READER' | 'ROLE_MEMBER' | 'ROLE_ADMIN'

interface AuthUser {
  id: number | null
  username: string | null
  firstname: string | null
  lastname: string | null
  email: string | null
  profileImage: string | null
  roles: Role[]
}

export interface RegisterPayload {
  username: string
  password: string
  firstname: string
  lastname: string
  email: string
  profileImage?: string | null
}

interface AuthResponsePayload {
  access_token?: string
  accessToken?: string
  refresh_token?: string
  refreshToken?: string
  token?: string
  user?: unknown
  [key: string]: unknown
}

const TOKEN_KEY = 'access_token'
const REFRESH_KEY = 'refresh_token'
const USER_KEY = 'user'
const LEGACY_KEYS = ['authData', 'auth', 'currentUser']
const ROLE_WHITELIST: Role[] = ['ROLE_READER', 'ROLE_MEMBER', 'ROLE_ADMIN']

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === 'object' && value !== null

const sanitiseStoredValue = (value: string | null): string | null => {
  if (!value) return null
  if (value === 'null' || value === 'undefined') return null
  return value
}

const normaliseRoles = (value: unknown): Role[] => {
  if (!Array.isArray(value)) {
    return []
  }
  return value.filter(
    (role): role is Role => typeof role === 'string' && ROLE_WHITELIST.includes(role as Role),
  )
}

const mapUser = (raw: unknown): AuthUser | null => {
  if (!isRecord(raw)) {
    return null
  }
  const roles = normaliseRoles(raw.roles)
  const rawId = raw.id
  const id =
    typeof rawId === 'number'
      ? rawId
      : rawId != null && !Number.isNaN(Number(rawId))
        ? Number(rawId)
        : null

  return {
    id,
    username: typeof raw.username === 'string' ? raw.username : null,
    firstname: typeof raw.firstname === 'string' ? raw.firstname : null,
    lastname: typeof raw.lastname === 'string' ? raw.lastname : null,
    email: typeof raw.email === 'string' ? raw.email : null,
    profileImage: typeof raw.profileImage === 'string' ? raw.profileImage : null,
    roles,
  }
}

const extractToken = (
  payload: AuthResponsePayload | Record<string, unknown>,
  type: 'access' | 'refresh',
): string | null => {
  const keys =
    type === 'access' ? ['access_token', 'accessToken', 'token'] : ['refresh_token', 'refreshToken']
  for (const key of keys) {
    const value = payload[key]
    if (typeof value === 'string' && value) {
      return value
    }
  }
  return null
}

const getLocalStorage = (): Storage | null => {
  if (typeof globalThis === 'undefined') {
    return null
  }
  if (!('localStorage' in globalThis)) {
    return null
  }
  try {
    return (globalThis as typeof globalThis & { localStorage: Storage }).localStorage
  } catch {
    return null
  }
}

interface SessionSnapshot {
  token: string | null
  refreshToken: string | null
  user: AuthUser | null
}

const loadSessionFromStorage = (): SessionSnapshot => {
  const storage = getLocalStorage()
  if (!storage) {
    return { token: null, refreshToken: null, user: null }
  }
  let storedToken =
    sanitiseStoredValue(storage.getItem(TOKEN_KEY)) ??
    sanitiseStoredValue(storage.getItem('accessToken'))
  let storedRefresh =
    sanitiseStoredValue(storage.getItem(REFRESH_KEY)) ??
    sanitiseStoredValue(storage.getItem('refreshToken'))
  const rawUser = storage.getItem(USER_KEY) ?? storage.getItem('authUser')
  let userFromStorage: AuthUser | null = null
  if (rawUser) {
    try {
      userFromStorage = mapUser(JSON.parse(rawUser))
    } catch (error) {
      console.warn('[auth-store] Failed to parse user JSON from storage', error)
    }
  }
  if (!userFromStorage) {
    for (const key of LEGACY_KEYS) {
      const legacyRaw = storage.getItem(key)
      if (!legacyRaw) continue
      try {
        const parsed = JSON.parse(legacyRaw) as Record<string, unknown>
        const legacyUser = mapUser(parsed.user ?? parsed)
        if (legacyUser) {
          userFromStorage = legacyUser
        }
        if (!storedToken) {
          storedToken = extractToken(parsed, 'access')
        }
        if (!storedRefresh) {
          storedRefresh = extractToken(parsed, 'refresh')
        }
        if (userFromStorage || storedToken || storedRefresh) {
          break
        }
      } catch (error) {
        console.warn(`[auth-store] Failed to parse legacy auth data for key "${key}"`, error)
      }
    }
  }
  return {
    token: storedToken,
    refreshToken: storedRefresh,
    user: userFromStorage,
  }
}


const initialSession = loadSessionFromStorage()

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: initialSession.token,
    refreshToken: initialSession.refreshToken,
    user: initialSession.user,
  }),

  getters: {
    roles(state): Role[] {
      return state.user?.roles ?? []
    },
    isAuthenticated(state): boolean {
      return !!state.token && !!state.user
    },
    authorizationHeader(state): string {
      return state.token ? `Bearer ${state.token}` : ''
    },
    currentUserName(state): string {
      const username = state.user?.username ?? ''
      const fullName = [state.user?.firstname, state.user?.lastname]
        .filter((part) => !!part)
        .join(' ')
        .trim()
      if (username && fullName) {
        return `${username} (${fullName})`
      }
      return username || fullName
    },
    hasRole(): (role: Role) => boolean {
      return (role: Role) => this.roles.includes(role)
    },
    hasAnyRole(): (required: Role[]) => boolean {
      return (required: Role[]) => required.some((role) => this.hasRole(role))
    },
    userProfileImage(state): string | null {
      return state.user?.profileImage ?? null
    },
  },

  actions: {
    async checkUsernameExists(username: string): Promise<boolean> {
      try {
        const response = await apiClient.get<{ isTaken: boolean }>('/api/v1/auth/check-username', {
          params: { username },
        })
        return response.data.isTaken
      } catch (error) {
        console.error('Error checking username:', error)
        return true
      }
    },

    async checkEmailExists(email: string): Promise<boolean> {
      try {
        const response = await apiClient.get<{ isTaken: boolean }>('/api/v1/auth/check-email', {
          params: { email },
        })
        return response.data.isTaken
      } catch (error) {
        console.error('Error checking email:', error)
        return true
      }
    },

    async register(payload: RegisterPayload) {
      return apiClient.post('/api/v1/auth/register', payload)
    },

    async login(identifier: string, password: string) {
      const { data } = await apiClient.post<AuthResponsePayload>('/api/v1/auth/authenticate', {
        identifier,
        password,
      })
      this.applyAuthResponse(data)
      return data
    },

    async forgotPassword(email: string) {
      try {
        await apiClient.post('/api/v1/auth/forgot-password', { email })
      } catch (error) {
        console.error('Forgot password request failed:', error)
        throw error
      }
    },

    logout() {
      this.setSession(null, null, null)
    },

    reload(token: string, user: AuthUser, refreshToken?: string | null) {
      this.setSession(token, refreshToken ?? this.refreshToken, user)
    },

    hydrateFromStorage() {
      const snapshot = loadSessionFromStorage()
      this.setSession(snapshot.token, snapshot.refreshToken, snapshot.user)
    },

    applyAuthResponse(payload: AuthResponsePayload) {
      if (!payload) {
        this.setSession(null, null, null)
        return
      }
      const user = mapUser(payload.user)
      const access = extractToken(payload, 'access')
      const refresh = extractToken(payload, 'refresh')
      this.setSession(access, refresh, user)
    },

    setSession(token: string | null, refreshToken: string | null, user: AuthUser | null) {
      this.token = token ?? null
      this.refreshToken = refreshToken ?? null
      this.user = user ?? null
      this.persistSession()

    },

    persistSession() {
      const storage = getLocalStorage()
      if (!storage) {
        return
      }
      if (this.token) {
        storage.setItem(TOKEN_KEY, this.token)
      } else {
        storage.removeItem(TOKEN_KEY)
      }
      if (this.refreshToken) {
        storage.setItem(REFRESH_KEY, this.refreshToken)
      } else {
        storage.removeItem(REFRESH_KEY)
      }
      if (this.user) {
        storage.setItem(USER_KEY, JSON.stringify(this.user))
      } else {
        storage.removeItem(USER_KEY)
      }
    },
  },
})
