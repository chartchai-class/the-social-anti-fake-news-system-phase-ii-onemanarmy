<script setup lang="ts">
import { ref } from 'vue';
import InputText from '../components/InputText.vue';
import * as yup from 'yup';
import { useField, useForm } from 'vee-validate';
import { useAuthStore } from '../stores/auth';
import { useRouter } from 'vue-router';
import { useMessageStore } from '../stores/message';

// --- Validation Schema ---
const validationSchema = yup.object({
    identifier: yup
        .string()
        .required('Username or Email is required'),
    password: yup
        .string()
        .required('Password is required')
});

// --- Form Handling (VeeValidate) ---
const { errors, handleSubmit } = useForm({
    validationSchema,
    initialValues: {
        identifier: "", // แก้ไขจาก username เป็น identifier
        password: ""
    }
});

// --- Form Fields ---
const { value: identifier } = useField<string>('identifier');
const { value: password } = useField<string>('password');

// --- Stores and Router ---
const authStore = useAuthStore();
const router = useRouter();
const messageStore = useMessageStore();
const isSubmitting = ref(false);
const messageType = ref<'success' | 'error' | null>(null);

// --- Submit Handler ---
const onSubmit = handleSubmit(async (values) => {
    isSubmitting.value = true;
    messageStore.resetMessage();

    try {
        await authStore.login(values.identifier, values.password);

        messageType.value = 'success';
        messageStore.updateMessage('Login successful!');
        setTimeout(() => messageStore.resetMessage(), 2000);

        await router.push({ name: 'home' });

    } catch (err: unknown) {
        messageType.value = 'error';
        let errorMessage = 'Login failed. Please try again.';

        if (typeof err === 'object' && err !== null && 'response' in err) {
            const apiError = err as { response?: { data?: { message?: string } } };
            errorMessage = apiError.response?.data?.message || 'Login failed. Invalid username or password.';
        }

        messageStore.updateMessage(errorMessage);
        setTimeout(() => messageStore.resetMessage(), 3000);

    } finally {
        isSubmitting.value = false;
    }
});
</script>

<template>
    <div class="flex min-h-full flex-1 flex-col justify-center py-12 sm:px-6 lg:px-8 bg-gray-70">

        <div v-if="messageStore.message"
            class="fixed top-5 right-5 z-50 p-4 rounded-md shadow-lg text-white transition-all duration-300"
            :class="messageType === 'success' ? 'bg-green-600' : 'bg-red-600'">
            {{ messageStore.message }}
        </div>

        <div class="sm:mx-auto sm:w-full sm:max-w-md">
            <img class="mx-auto h-16 w-auto" src="/logo.png" alt="3Clouds News Logo" />
        </div>

        <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
            <div class="bg-white px-4 py-8 shadow-lg sm:rounded-lg sm:px-10">

                <h2 class="text-center text-2xl font-bold leading-9 tracking-tight text-gray-900">
                    Sign in to 3Clouds News
                </h2>

                <form class="mt-8 space-y-6" @submit.prevent="onSubmit" novalidate>
                    <div>
                        <label for="identifier" class="block text-sm font-medium text-left leading-6 text-gray-900">
                            Username or Email
                        </label>
                        <div class="mt-2">
                            <InputText id="identifier" type="text" v-model="identifier" placeholder="Username or Email"
                                :error="errors['identifier']" />
                        </div>
                    </div>

                    <div>
                        <div class="flex items-center justify-between">
                            <label for="password" class="block text-sm font-medium leading-6 text-gray-900">
                                Password
                            </label>
                            <div class="text-sm">
                                <a href="#" class="font-semibold text-blue-600 hover:text-blue-500">Forgot password?</a>
                            </div>
                        </div>
                        <div class="mt-2">
                            <InputText id="password" v-model="password" type="password" placeholder="Your Password"
                                :error="errors['password']" />
                        </div>
                    </div>

                    <div>
                        <button type="submit" :disabled="isSubmitting"
                            class="flex w-full justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold leading-6 text-white shadow-sm hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600 disabled:opacity-50 transition-colors duration-300">
                            <span v-if="isSubmitting"
                                class="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></span>
                            Sign in
                        </button>
                    </div>
                </form>

                <div class="mt-6">
                    <div class="relative">
                        <div class="absolute inset-0 flex items-center">
                            <div class="w-full border-t border-gray-300" />
                        </div>
                        <div class="relative flex justify-center text-sm">
                            <span class="bg-white px-2 text-gray-500">Or</span>
                        </div>
                    </div>
                </div>

                <p class="mt-6 text-center text-sm text-gray-500">
                    Don't have an account?
                    {{ ' ' }}
                    <RouterLink :to="{ name: 'register' }"
                        class="font-semibold leading-6 text-blue-600 hover:text-blue-500">
                        Register now
                    </RouterLink>
                </p>

            </div>
        </div>
    </div>
</template>