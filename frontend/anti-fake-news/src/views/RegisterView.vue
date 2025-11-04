<script setup lang="ts">
import { ref } from 'vue';
import InputText from '@/components/InputText.vue';
import * as yup from 'yup';
import { useField, useForm } from 'vee-validate';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import { useMessageStore } from '@/stores/message';
import type { RegisterPayload } from '@/stores/auth';
import apiClient from '@/services/AxiosClient'

const messageType = ref<'success' | 'error' | null>(null);
const defaultProfile = 'https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_640.png';
const fileInput = ref<HTMLInputElement | null>(null);
const profilePreview = ref<string | null>(null);

function triggerFileInput() {
    fileInput.value?.click();
}

async function handleProfileUpload(event: Event) {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0];
    if (!file) return;

    // preview
    const reader = new FileReader();
    reader.onload = (e) => {
        profilePreview.value = e.target?.result as string;
    };
    reader.readAsDataURL(file);

    // upload to backend
    try {
        const formData = new FormData();
        formData.append('file', file);

        const response = await apiClient.post(
            'http://localhost:8080/uploadFile',
            formData,
            { headers: { 'Content-Type': 'multipart/form-data' } }
        );

        profileImage.value = response.data; // ใช้ URL จาก backend
        console.log('Uploaded file URL:', response.data);

    } catch (err) {
        console.error('Upload error:', err);
        profileImage.value = defaultProfile; // fallback เป็น default ถ้า error
    }
}

// --- Validation Schema ---
const validationSchema = yup.object({
    firstname: yup.string().required('First name is required'),
    lastname: yup.string().required('Last name is required'),
    email: yup.string().required('Email is required').email('Must be a valid email'),
    username: yup.string().required('Username is required').min(3, 'Username must be at least 3 characters'),
    password: yup.string().required('Password is required').min(6, 'Password must be at least 6 characters'),
    confirmPassword: yup.string()
        .required('Please confirm your password')
        .oneOf([yup.ref('password')], 'Passwords must match'),
    profileImage: yup.string()
        // .url('Must be a valid URL (e.g., http://...)')
        .default(defaultProfile)
        .required()
});

// --- Form Handling ---
const { errors, handleSubmit, setFieldError } = useForm({
    validationSchema,
    initialValues: {
        firstname: "",
        lastname: "",
        email: "",
        username: "",
        password: "",
        confirmPassword: "",
        profileImage: defaultProfile
    }
});

// --- Form Fields ---
const { value: firstname } = useField<string>('firstname');
const { value: lastname } = useField<string>('lastname');
const { value: email } = useField<string>('email');
const { value: username } = useField<string>('username');
const { value: password } = useField<string>('password');
const { value: confirmPassword } = useField<string>('confirmPassword');
const { value: profileImage } = useField<string>('profileImage');

// --- Stores and Router ---
const authStore = useAuthStore();
const router = useRouter();
const messageStore = useMessageStore();
const isSubmitting = ref(false);

// --- Availability Check State ---
const isCheckingUsername = ref(false);
const isCheckingEmail = ref(false);

// --- Availability Check Functions ---
async function checkUsernameAvailability() {
    try {
        await validationSchema.validateAt('username', { username: username.value });
        setFieldError('username', undefined); // Clear previous errors if basic validation passes
    } catch (validationError: any) {
        setFieldError('username', validationError.message);
        return;
    }
    isCheckingUsername.value = true;
    try {
        const isTaken = await authStore.checkUsernameExists(username.value);
        if (isTaken) {
            setFieldError('username', 'Username is already taken.');
        } else {
            setFieldError('username', undefined); // Explicitly clear if not taken
        }
    } catch (error) {
        console.error("Error during username check:", error);
        setFieldError('username', 'Could not check username availability.');
    } finally {
        isCheckingUsername.value = false;
    }
}

async function checkEmailAvailability() {
    try {
        await validationSchema.validateAt('email', { email: email.value });
        setFieldError('email', undefined); // Clear previous errors
    } catch (validationError: any) {
        setFieldError('email', validationError.message);
        return;
    }
    isCheckingEmail.value = true;
    try {
        const isTaken = await authStore.checkEmailExists(email.value);
        if (isTaken) {
            setFieldError('email', 'Email is already registered.');
        } else {
            setFieldError('email', undefined); // Explicitly clear if not taken
        }
    } catch (error) {
        console.error("Error during email check:", error);
        setFieldError('email', 'Could not check email availability.');
    } finally {
        isCheckingEmail.value = false;
    }
}

// --- Submit Handler ---
const onSubmit = handleSubmit(async (values) => {
    // Check errors again right before submitting, just in case
    if (Object.keys(errors.value).length > 0) {
        messageType.value = 'error';
        messageStore.updateMessage('Please fix the errors in the form before submitting.');
        setTimeout(() => messageStore.resetMessage(), 3000);
        return;
    }

    isSubmitting.value = true;
    messageStore.resetMessage();

    const payload: RegisterPayload = {
        firstname: values.firstname,
        lastname: values.lastname,
        email: values.email,
        username: values.username,
        password: values.password,
        profileImage: profileImage.value || defaultProfile
    };

    try {
        await authStore.register(payload);

        messageType.value = 'success';
        messageStore.updateMessage('Registration successful! Please sign in.');
        setTimeout(() => {
            messageStore.resetMessage();
            router.push({ name: 'signin' });
        }, 1000);

    } catch (err: unknown) {
        messageType.value = 'error';
        let errorMessage = 'Registration failed. Please try again.';
        if (typeof err === 'object' && err !== null && 'response' in err) {
            const apiError = err as { response?: { data?: { message?: string } } };
            errorMessage = apiError.response?.data?.message || 'Registration failed. Username or Email might already exist.';
        } else if (err instanceof Error) {
            errorMessage = err.message;
        }
        messageStore.updateMessage(errorMessage);
        setTimeout(() => messageStore.resetMessage(), 3000);
        console.error('Registration error:', err);
        isSubmitting.value = false; // Stop loading on error
    }
});
</script>

<template>
  <div
    class="flex min-h-full flex-1 flex-col justify-center py-12 sm:px-6 lg:px-8 bg-gradient-to-b from-black via-gray-900 to-black font-oswald"
  >
    <div
      v-if="messageStore.message"
      class="fixed top-5 right-5 z-50 p-4 rounded-md shadow-lg text-white transition-all duration-300"
      :class="messageType === 'success' ? 'bg-green-600' : 'bg-red-600'"
    >
      {{ messageStore.message }}
    </div>

    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <img class="mx-auto h-16 w-auto" src="/logo.png" alt="One Man Army News Logo" />
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white px-4 py-8 shadow-lg sm:rounded-lg sm:px-10">
        <h2 class="text-center text-2xl font-bold leading-9 tracking-tight text-gray-900">
          Create your account
        </h2>

        <div class="flex justify-center mt-6 flex-col items-center">
          <div class="relative">
            <img
              :src="profilePreview || profileImage || defaultProfile"
              class="w-24 h-24 rounded-full object-cover border-2 border-gray-300 cursor-pointer hover:border-nike-orange transition-all"
              @click="triggerFileInput"
            />
            <input
              type="file"
              accept="image/*"
              ref="fileInput"
              class="hidden"
              @change="handleProfileUpload"
            />
          </div>
          <p class="text-sm text-gray-500 mt-2 text-center">Upload your profile picture</p>
        </div>

        <form class="mt-8 space-y-6" @submit.prevent="onSubmit" novalidate>
          <div>
            <label
              for="firstname"
              class="block text-sm font-medium text-left leading-6 text-gray-900"
              >First Name</label
            >
            <div class="mt-2">
              <InputText
                id="firstname"
                type="text"
                v-model="firstname"
                placeholder="Your First Name"
                :error="errors.firstname || undefined"
                autocomplete="given-name"
                class="focus:ring-nike-orange"
              />
            </div>
          </div>

          <div>
            <label
              for="lastname"
              class="block text-sm font-medium text-left leading-6 text-gray-900"
              >Last Name</label
            >
            <div class="mt-2">
              <InputText
                id="lastname"
                type="text"
                v-model="lastname"
                placeholder="Your Last Name"
                :error="errors.lastname || undefined"
                autocomplete="family-name"
                class="focus:ring-nike-orange"
              />
            </div>
          </div>

          <div>
            <label for="email" class="block text-sm font-medium text-left leading-6 text-gray-900"
              >Email Address</label
            >
            <div class="mt-2 relative">
              <InputText
                id="email"
                type="email"
                v-model="email"
                placeholder="Example@email.com"
                :error="errors.email || undefined"
                autocomplete="email"
                @blur="checkEmailAvailability"
                @input="() => setFieldError('email', undefined)"
                class="focus:ring-nike-orange"
              />
              <div
                v-if="isCheckingEmail"
                class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none"
              >
                <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-gray-400"></div>
              </div>
            </div>
          </div>

          <div>
            <label
              for="username"
              class="block text-sm font-medium text-left leading-6 text-gray-900"
              >Username</label
            >
            <div class="mt-2 relative">
              <InputText
                id="username"
                type="text"
                v-model="username"
                placeholder="Choose a username"
                :error="errors.username || undefined"
                autocomplete="username"
                @blur="checkUsernameAvailability"
                @input="() => setFieldError('username', undefined)"
                class="focus:ring-nike-orange"
              />
              <div
                v-if="isCheckingUsername"
                class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none"
              >
                <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-gray-400"></div>
              </div>
            </div>
          </div>

          <div>
            <label
              for="password"
              class="block text-sm font-medium text-left leading-6 text-gray-900"
              >Password</label
            >
            <div class="mt-2">
              <InputText
                id="password"
                type="password"
                v-model="password"
                placeholder="Create a password"
                :error="errors.password || undefined"
                autocomplete="new-password"
                class="focus:ring-nike-orange"
              />
            </div>
          </div>

          <div>
            <label
              for="confirmPassword"
              class="block text-sm font-medium text-left leading-6 text-gray-900"
              >Confirm Password</label
            >
            <div class="mt-2">
              <InputText
                id="confirmPassword"
                type="password"
                v-model="confirmPassword"
                placeholder="Confirm your password"
                :error="errors.confirmPassword || undefined"
                autocomplete="new-password"
                class="focus:ring-nike-orange"
              />
            </div>
          </div>

          <div>
            <button
              type="submit"
              :disabled="
                isSubmitting || Object.keys(errors).filter((k) => k !== 'profileImage').length > 0
              "
              class="flex w-full justify-center rounded-lg bg-nike-orange px-3 py-3 text-sm font-semibold leading-6 text-white shadow-lg hover:bg-nike-yellow hover:text-nike-black focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-orange-500 disabled:opacity-50 transition-all duration-300 transform hover:scale-105"
            >
              <span
                v-if="isSubmitting"
                class="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"
              ></span>
              Register
            </button>
          </div>
        </form>

        <p class="mt-8 text-center text-sm text-gray-500">
          Already have an account?
          {{ ' ' }}
          <RouterLink
            :to="{ name: 'signin' }"
            class="font-semibold leading-6 text-nike-orange hover:text-nike-yellow"
          >
            Sign in here
          </RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>
