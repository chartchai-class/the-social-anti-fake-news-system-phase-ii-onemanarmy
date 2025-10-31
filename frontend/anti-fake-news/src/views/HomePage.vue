<script setup lang="ts">
import { ref, computed, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NewsCard from '@/components/NewsCard.vue'
import Pagination from '@/components/PaginationButtons.vue'
import BaseInput from '@/components/SearchBar.vue'
import type { News } from '../stores/newsInterface'
import { useAuthStore } from '../stores/auth'
import apiClient from '../services/NewsService'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.hasRole('ROLE_ADMIN'))

type FilterStatus = 'all' | 'fake' | 'not fake' | 'equal' | 'removed'

const filterStatus = ref<FilterStatus>((route.query.filter as FilterStatus) || 'all')
const currentPage = ref<number>(Number(route.query.page) || 1)
const newsPerPage = ref<number>(Number(route.query.perPage) || 6)
const searchQuery = ref<string>((route.query.search as string) || '') // 🔍 keyword search
const statusSearchQuery = ref<string>((route.query.statusSearch as string) || '') // status search

const isDataLoading = ref(false)
const totalItems = ref(0)
const newsList = ref<News[]>([])

const calculateNewsStatus = (news: News): 'fake' | 'not fake' | 'equal' | 'removed' => {
  if (news.status === 'removed' || news.removed) {
    return 'removed'
  }

  const realVotes = news.voteSummary?.real || 0
  const fakeVotes = news.voteSummary?.fake || 0

  if (realVotes > fakeVotes) {
    return 'not fake'
  } else if (realVotes < fakeVotes) {
    return 'fake'
  } else {
    return 'equal'
  }
}

const handleKeywordEnter = () => {
  updateSearchQuery('keyword')
}

const handleStatusEnter = () => {
  updateSearchQuery('status')
}

function updateSearchQuery(type: 'keyword' | 'status') {
  currentPage.value = 1

  const newQuery: Record<string, any> = {
    page: 1,
    perPage: newsPerPage.value,
    filter: filterStatus.value !== 'all' ? filterStatus.value : undefined,
  }

  // เก็บค่า search ทั้งสองแบบ
  if (type === 'keyword') {
    newQuery.search = searchQuery.value || undefined
    newQuery.statusSearch = statusSearchQuery.value || undefined
  } else if (type === 'status') {
    newQuery.search = searchQuery.value || undefined
    newQuery.statusSearch = statusSearchQuery.value || undefined
  }

  // ลบ key ที่มีค่า undefined
  if (newQuery.search === undefined) delete newQuery.search
  if (newQuery.statusSearch === undefined) delete newQuery.statusSearch

  // เปลี่ยน URL
  router.push({
    path: route.path,
    query: newQuery,
  })
}

/**
 * ล้าง search query
 */
function clearSearchQuery(type: 'keyword' | 'status') {
  if (type === 'keyword') {
    searchQuery.value = ''
  } else if (type === 'status') {
    statusSearchQuery.value = ''
  }

  const newQuery: Record<string, any> = {
    page: 1,
    perPage: newsPerPage.value,
    filter: filterStatus.value !== 'all' ? filterStatus.value : undefined,
  }

  if (type === 'keyword') {
    newQuery.search = undefined
    newQuery.statusSearch = statusSearchQuery.value || undefined
  } else if (type === 'status') {
    newQuery.search = searchQuery.value || undefined
    newQuery.statusSearch = undefined
  }

  router.push({
    path: route.path,
    query: newQuery,
  })
}

/**
 * เปลี่ยน filter button (All, Real, Fake, Equal, Removed)
 */
function handleFilterChange(newFilter: FilterStatus) {
  if (filterStatus.value === newFilter) return // ถ้าเป็นปุ่มเดิม ไม่ทำอะไร

  if (newFilter === 'removed' && !isAdmin.value) return // ถ้าไม่ใช่ admin ห้ามกดปุ่ม Removed

  filterStatus.value = newFilter
  currentPage.value = 1 // กลับไปหน้าแรก

  const newQuery: Record<string, any> = {
    page: 1,
    perPage: newsPerPage.value,
    filter: newFilter !== 'all' ? newFilter : undefined,
    search: searchQuery.value || undefined,
    statusSearch: statusSearchQuery.value || undefined,
  }

  router.push({
    path: route.path,
    query: newQuery,
  })
}

/**
 * เปลี่ยนจำนวนข่าวต่อหน้า
 */
function updatePerPage() {
  currentPage.value = 1

  const newQuery: Record<string, any> = {
    page: 1,
    perPage: newsPerPage.value,
    filter: filterStatus.value !== 'all' ? filterStatus.value : undefined,
    search: searchQuery.value || undefined,
    statusSearch: statusSearchQuery.value || undefined,
  }

  router.push({
    path: route.path,
    query: newQuery,
  })
}

/**
 * เปลี่ยนหน้า pagination
 */
function onPageChanged(page: number) {
  currentPage.value = page

  const newQuery: Record<string, any> = {
    page: page,
    perPage: newsPerPage.value,
    filter: filterStatus.value !== 'all' ? filterStatus.value : undefined,
    search: searchQuery.value || undefined,
    statusSearch: statusSearchQuery.value || undefined,
    sort: sortOrder.value,
  }

  router.push({
    path: route.path,
    query: newQuery,
  })

  window.scrollTo({ top: 0, behavior: 'smooth' }) // เลื่อนขึ้นบนสุด
}

// สถานะ Sorting
const sortOrder = ref<'newest' | 'oldest' | 'mostVoted' | 'mostCommented'>('newest')

// Computed ข่าวพร้อมกรองและ sort
const sortedNewsList = computed(() => {
  const annotated = newsListWithStatus.value.map((item, index) => ({ item, index }))

  const sortBy = (items: typeof annotated) => {
    switch (sortOrder.value) {
      case 'newest':
        return [...items].sort(
          (a, b) => new Date(b.item.dateTime).getTime() - new Date(a.item.dateTime).getTime()
        )
      case 'oldest':
        return [...items].sort(
          (a, b) => new Date(a.item.dateTime).getTime() - new Date(b.item.dateTime).getTime()
        )
      case 'mostVoted':
        return [...items].sort(
          (a, b) =>
            (b.item.voteSummary?.real || 0) + (b.item.voteSummary?.fake || 0) -
            ((a.item.voteSummary?.real || 0) + (a.item.voteSummary?.fake || 0))
        )
      case 'mostCommented':
        return [...items].sort(
          (a, b) => (b.item.comments?.length || 0) - (a.item.comments?.length || 0)
        )
      default:
        return items
    }
  }

  const sorted = sortBy(annotated)

  const sortedWithPosition = sorted.map((entry, position) => ({
    ...entry,
    position,
  }))

  const removedEntries = sortedWithPosition.filter(
    ({ item }) => item.status === 'removed' || item.removed
  )

  if (!removedEntries.length) {
    return sorted.map(({ item }) => item)
  }

  const withoutRemoved = sortedWithPosition.filter(
    ({ item }) => item.status !== 'removed' && !item.removed
  )

  const restoredOrder = [...withoutRemoved]

  removedEntries.forEach((entry) => {
    const insertAt = Math.min(entry.position, restoredOrder.length)
    restoredOrder.splice(insertAt, 0, entry)
  })

  return restoredOrder.map(({ item }) => item)
})

function onSortChange() {
  currentPage.value = 1 // กลับไปหน้าแรก
  router.push({
    path: route.path,
    query: {
      page: 1,
      perPage: newsPerPage.value,
      filter: filterStatus.value !== 'all' ? filterStatus.value : undefined,
      search: searchQuery.value || undefined,
      statusSearch: statusSearchQuery.value || undefined,
      sort: sortOrder.value, // ส่ง param ใหม่
    },
  })
}

/**
 * watchEffect: ทำงานทุกครั้งที่ URL query เปลี่ยน
 * หน้าที่: ดึงข้อมูลข่าวจาก Backend
 */
watchEffect(() => {
  // อ่านค่าจาก URL
  const pageValue = Number(route.query.page) || 1
  const perPageValue = Number(route.query.perPage) || 6
  const filterValue = (route.query.filter as FilterStatus) || 'all'
  const searchValue = (route.query.search as string) || ''
  const statusSearchValue = (route.query.statusSearch as string) || ''
  const sortValue = (route.query.sort as 'newest' | 'oldest' | 'mostVoted' | 'mostCommented') || 'newest'

  // Sync ค่ากับ local state
  currentPage.value = pageValue
  newsPerPage.value = perPageValue
  filterStatus.value = filterValue
  searchQuery.value = searchValue
  statusSearchQuery.value = statusSearchValue
  sortOrder.value = sortValue

  // เตรียม parameters สำหรับส่งไป Backend
  const params: any = {
    _page: pageValue, // แปลง page → _page
    _limit: perPageValue, // แปลง perPage → _limit
  }

    // SORT: ส่ง sort parameter ไปให้ Backend
  if (sortValue === 'newest') {
    params._sort = 'dateTime'
    params._order = 'desc'
  } else if (sortValue === 'oldest') {
    params._sort = 'dateTime'
    params._order = 'asc'
  } else if (sortValue === 'mostVoted') {
    params._sort = 'totalVotes'
    params._order = 'desc'
  } else if (sortValue === 'mostCommented') {
    params._sort = 'commentCount'
    params._order = 'desc'
  }

  // SEARCH: ถ้ามี keyword search ส่ง parameter "title"
  if (searchValue) {
    params.title = searchValue
  }

  // FILTER: ถ้ามี status search ส่ง parameter "status"
  if (statusSearchValue) {
    // กรณี 1: ผู้ใช้พิมพ์ในช่อง Status Search ("real", "fake", "equal", หรือ "removed")
    params.status = statusSearchValue
  } else if (filterValue !== 'all') {
    // ถ้าไม่มี status search แต่มี filter button
    // แปลง "not fake" → "real" สำหรับ Backend
    if (filterValue === 'not fake') {
      params.status = 'real'
    } else {
      params.status = filterValue // ← ตรงนี้รวม 'removed' ด้วย!

    // วิธีที่ 1: Filter Button "Removed"
    //   - กดปุ่ม Removed (เฉพาะ Admin)
    //   - ส่ง → params.status = 'removed'
    //
    // วิธีที่ 2: Status Search พิมพ์ "removed"
    //   - พิมพ์คำว่า "removed" ในช่อง Status Search
    //   - ส่ง → params.status = 'removed'
    // ทั้งสองวิธีใช้ backend endpoint, เดียวกัน Backend รับ parameter "status" และกรองข่าวที่ถูกลบ
    }
  }

    // เพิ่ม includeRemoved สำหรับ Admin เมื่อดูข่าวที่ถูกลบ
  if (filterValue === 'removed' || statusSearchValue === 'removed') {
    params.includeRemoved = true
  }

  // เริ่มโหลดข้อมูล
  isDataLoading.value = true

  console.log('Calling /api/news/search with params:', params)

  // เรียก Backend API
  apiClient
  .searchNews(params)
  .then((response) => {
    // แก้ตรงนี้: ตรวจสอบว่าเป็น array ก่อน
    newsList.value = Array.isArray(response.data) ? response.data : []
    totalItems.value = parseInt(response.headers['x-total-count']) || newsList.value.length
    console.log('Received:', newsList.value.length, 'items')
  })
  .catch((error) => {
    console.error('Error:', error)
    newsList.value = []
    totalItems.value = 0
  })
  .finally(() => {
    isDataLoading.value = false
  })
})

/**
 * Computed: เพิ่ม status ให้กับข่าว (สำหรับแสดงแถบสี)
 */
const newsListWithStatus = computed(() => {
  return newsList.value.map((news) => ({
    ...news,
    status: calculateNewsStatus(news),
  }))
})

/**
 * Handler: เมื่อลบข่าวสำเร็จ
 */
const handleNewsRemoved = (removedNewsId: number) => {
  const index = newsList.value.findIndex((news) => news.id === removedNewsId)
  if (index === -1) {
    return
  }

  const original = newsList.value[index]
  const updated: News = {
    ...original,
    status: 'removed',
    removed: true,
  }

  newsList.value.splice(index, 1, updated)
}
</script>

<template>
  <div
    class="container mx-auto p-4 font-oswald bg-transparent"
  >
    <div
      class="flex flex-col md:flex-row justify-between items-center mb-6 space-y-4 md:space-y-0"
    >
      <div class="flex space-x-2">
        <button
          @click="handleFilterChange('all')"
          :disabled="isDataLoading"
          :class="[
            'px-4 py-2 rounded-lg font-medium transition-colors duration-200 relative ',
            {
              'bg-nike-orange text-white': filterStatus === 'all',
              'bg-white text-gray-800 hover:bg-nike-yellow hover:text-white':
                filterStatus !== 'all',
              'opacity-75 cursor-not-allowed': isDataLoading
            }
          ]"
        >
          <span :class="{ 'opacity-50': isDataLoading }">All</span>
          <div v-if="isDataLoading" class="absolute inset-0 flex items-center justify-center">
          </div>
        </button>

        <button
          @click="handleFilterChange('not fake')"
          :disabled="isDataLoading"
          :class="[
            'px-4 py-2 rounded-lg font-medium border transition-colors duration-200 relative',
            {
              'bg-green-600 text-white': filterStatus === 'not fake',
              'bg-white text-gray-800 hover:bg-green-500 hover:text-white':
                filterStatus !== 'not fake',
              'opacity-75 cursor-not-allowed': isDataLoading
            }
          ]"
        >
          <span :class="{ 'opacity-50': isDataLoading }">Real</span>
          <div v-if="isDataLoading" class="absolute inset-0 flex items-center justify-center">
          </div>
        </button>

        <button
          @click="handleFilterChange('fake')"
          :disabled="isDataLoading"
          :class="[
            'px-4 py-2 rounded-lg font-medium border transition-colors duration-200 relative',
            {
              'bg-red-600 text-white': filterStatus === 'fake',
              'bg-white text-gray-800 hover:bg-red-500 hover:text-white': filterStatus !== 'fake',
              'opacity-75 cursor-not-allowed': isDataLoading
            }
          ]"
        >
          <span :class="{ 'opacity-50': isDataLoading }">Fake</span>
          <div v-if="isDataLoading" class="absolute inset-0 flex items-center justify-center">
          </div>
        </button>

        <button
          @click="handleFilterChange('equal')"
          :disabled="isDataLoading"
          :class="[
            'px-4 py-2 rounded-lg font-medium border transition-colors duration-200 relative',
            {
              'bg-gray-600 text-white': filterStatus === 'equal',
              'bg-white text-gray-800 hover:bg-gray-500 hover:text-white':
                filterStatus !== 'equal',
              'opacity-75 cursor-not-allowed': isDataLoading
            }
          ]"
        >
          <span :class="{ 'opacity-50': isDataLoading }">Equal</span>
          <div v-if="isDataLoading" class="absolute inset-0 flex items-center justify-center">
          </div>
        </button>

        <button
          v-if="isAdmin"
          @click="handleFilterChange('removed')"
          :disabled="isDataLoading"
          :class="[
            'px-4 py-2 rounded-lg font-medium border transition-colors duration-200 relative',
            {
              'bg-rose-600 text-white': filterStatus === 'removed',
              'bg-white text-gray-800 hover:bg-rose-500 hover:text-white':
                filterStatus !== 'removed',
              'opacity-75 cursor-not-allowed': isDataLoading
            }
          ]"
        >
          <span :class="{ 'opacity-50': isDataLoading }">Removed</span>
          <div v-if="isDataLoading" class="absolute inset-0 flex items-center justify-center">
            <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
          </div>
        </button>
      </div>

      <div class="flex items-center justify-end space-x-4">
        <div class="flex items-center space-x-2">
          <span class="text-white">Sort by:</span>
          <select
            v-model="sortOrder"
            @change="onSortChange"
            :disabled="isDataLoading"
            class="p-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-nike-orange"
          >
            <option value="newest">Newest</option>
            <option value="oldest">Oldest</option>
            <option value="mostVoted">Most Vote</option>
            <option value="mostCommented">Most Comment</option>
          </select>
        </div>

        <div class="flex items-center space-x-2">
          <span class="text-white">News per page:</span>
          <select
            v-model="newsPerPage"
            @change="updatePerPage"
            :disabled="isDataLoading"
            class="p-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-nike-orange disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <option>3</option>
            <option>6</option>
            <option>9</option>
            <option>12</option>
            <option>24</option>
          </select>
        </div>
      </div>
    </div>

    <div class="mb-6 space-y-3">
      <div class="flex flex-col sm:flex-row gap-3">

        <div class="flex-1">
          <BaseInput
            v-model="searchQuery"
            placeholder="Search by topic, detail, or reporter..."
            :disabled="isDataLoading"
            @keyup.enter="handleKeywordEnter"
          />
        </div>

        <div class="flex-1">
          <div class="relative">
            <input
              v-model="statusSearchQuery"
              type="text"
              placeholder="Search by status..."
              class="w-full p-3 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-nike-orange"
              :disabled="isDataLoading"
              @keyup.enter="handleStatusEnter"
            />
            <button
              v-if="statusSearchQuery"
              @click="clearSearchQuery('status')"
              class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors"
              :disabled="isDataLoading"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                ></path>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <div
        v-if="route.query.search || route.query.statusSearch"
        class="flex flex-wrap gap-2 items-center"
      >
        <span class="text-sm text-gray-300">Active filters:</span>
        <span
          v-if="route.query.search"
          class="inline-flex items-center px-3 py-1 rounded-full text-sm bg-blue-100 text-blue-800"
        >
          Keyword: "{{ route.query.search }}"
          <button @click="clearSearchQuery('keyword')" class="ml-2 hover:text-blue-900">
            <svg classs="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </span>
        <span
          v-if="route.query.statusSearch"
          class="inline-flex items-center px-3 py-1 rounded-full text-sm bg-purple-100 text-purple-800"
        >
          Status: "{{ route.query.statusSearch }}"
          <button @click="clearSearchQuery('status')" class="ml-2 hover:text-purple-900">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </span>
      </div>
    </div>

    <div v-if="isDataLoading" class="text-center text-white text-xl py-20">
      <div class="inline-flex flex-col items-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-yellow-500 mb-4"></div>
        <span>Loading news...</span>
      </div>
    </div>

    <div
      v-else-if="sortedNewsList.length"
      class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
    >
      <NewsCard
        v-for="news in sortedNewsList"
        :key="news.id"
        :news="news"
        :search-query="searchQuery"
        @news-removed="handleNewsRemoved"
      />
    </div>

    <div v-else class="text-center text-gray-500 text-xl py-10">
      <div class="flex flex-col items-center">
        <svg class="w-16 h-16 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
          ></path>
        </svg>
        <h3 class="text-lg font-medium mb-1">No news found</h3>
        <p class="text-sm text-gray-400">Try changing the filter or search terms.</p>
      </div>
    </div>

    <Pagination
      v-if="!isDataLoading && newsListWithStatus.length > 0"
      :total-items="totalItems"
      :items-per-page="newsPerPage"
      :current-page="currentPage"
      @page-changed="onPageChanged"
      class="mt-8"
    />

    <div class="text-center mt-6">
      <p class="text-sm text-white">© One Man Army - All rights reserved.</p>
    </div>
  </div>
</template>

