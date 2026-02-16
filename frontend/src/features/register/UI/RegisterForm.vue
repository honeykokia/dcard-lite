<script setup lang="ts">
import { useRegister } from '../model/useRegister';

const {
  form,
  errors,
  isLoading,
  apiError,
  isValid,
  validateName,
  validateEmail,
  validatePassword,
  validateConfirmPassword,
  handleRegister
} = useRegister();
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-[#f2f3f5] px-4 py-8">
    <div class="w-full max-w-md bg-white rounded-2xl shadow-lg p-10">
      <!-- Logo -->
      <div class="flex justify-center mb-8">
        <div class="w-20 h-20 bg-[#3397cf] rounded-full flex items-center justify-center">
          <span class="text-white text-4xl font-bold">D</span>
        </div>
      </div>

      <!-- Title -->
      <h1 class="text-3xl font-semibold text-center text-gray-800 mb-8">
        Register Account
      </h1>

      <!-- API Error Alert -->
      <div
        v-if="apiError"
        class="flex items-center gap-3 bg-red-50 border border-red-200 text-red-600 rounded-lg p-4 mb-6 text-sm"
      >
        <svg class="w-5 h-5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
          <path
            fill-rule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
            clip-rule="evenodd"
          />
        </svg>
        <span>{{ apiError }}</span>
      </div>

      <!-- Register Form -->
      <form @submit.prevent="handleRegister" class="space-y-6">
        <!-- Name Field -->
        <div class="space-y-2">
          <label for="name" class="block text-sm font-medium text-gray-600">
            Name
          </label>
          <input
            id="name"
            v-model="form.name"
            type="text"
            class="w-full px-4 py-3 text-base text-gray-800 bg-white border rounded-lg transition-all duration-200 focus:outline-none focus:ring-3"
            :class="
              errors.name.length > 0
                ? 'border-red-500 focus:border-red-500 focus:ring-red-100'
                : 'border-gray-300 focus:border-[#3397cf] focus:ring-[#3397cf]/15'
            "
            :disabled="isLoading"
            @blur="validateName"
            placeholder="Enter your name"
            autocomplete="name"
          />
          <span v-if="errors.name.length > 0" class="block text-sm text-red-500 mt-1">
            {{ errors.name[0] }}
          </span>
        </div>

        <!-- Email Field -->
        <div class="space-y-2">
          <label for="email" class="block text-sm font-medium text-gray-600">
            Email
          </label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            class="w-full px-4 py-3 text-base text-gray-800 bg-white border rounded-lg transition-all duration-200 focus:outline-none focus:ring-3"
            :class="
              errors.email.length > 0
                ? 'border-red-500 focus:border-red-500 focus:ring-red-100'
                : 'border-gray-300 focus:border-[#3397cf] focus:ring-[#3397cf]/15'
            "
            :disabled="isLoading"
            @blur="validateEmail"
            placeholder="Enter your email"
            autocomplete="email"
          />
          <span v-if="errors.email.length > 0" class="block text-sm text-red-500 mt-1">
            {{ errors.email[0] }}
          </span>
        </div>

        <!-- Password Field -->
        <div class="space-y-2">
          <label for="password" class="block text-sm font-medium text-gray-600">
            Password
          </label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            class="w-full px-4 py-3 text-base text-gray-800 bg-white border rounded-lg transition-all duration-200 focus:outline-none focus:ring-3"
            :class="
              errors.password.length > 0
                ? 'border-red-500 focus:border-red-500 focus:ring-red-100'
                : 'border-gray-300 focus:border-[#3397cf] focus:ring-[#3397cf]/15'
            "
            :disabled="isLoading"
            @blur="validatePassword"
            placeholder="Enter your password"
            autocomplete="new-password"
          />
          <span v-if="errors.password.length > 0" class="block text-sm text-red-500 mt-1">
            {{ errors.password[0] }}
          </span>
        </div>

        <!-- Confirm Password Field -->
        <div class="space-y-2">
          <label for="confirmPassword" class="block text-sm font-medium text-gray-600">
            Confirm Password
          </label>
          <input
            id="confirmPassword"
            v-model="form.confirmPassword"
            type="password"
            class="w-full px-4 py-3 text-base text-gray-800 bg-white border rounded-lg transition-all duration-200 focus:outline-none focus:ring-3"
            :class="
              errors.confirmPassword.length > 0
                ? 'border-red-500 focus:border-red-500 focus:ring-red-100'
                : 'border-gray-300 focus:border-[#3397cf] focus:ring-[#3397cf]/15'
            "
            :disabled="isLoading"
            @blur="validateConfirmPassword"
            placeholder="Confirm your password"
            autocomplete="new-password"
          />
          <span v-if="errors.confirmPassword.length > 0" class="block text-sm text-red-500 mt-1">
            {{ errors.confirmPassword[0] }}
          </span>
        </div>

        <!-- Submit Button -->
        <button
          type="submit"
          class="w-full px-4 py-3.5 text-base font-semibold text-white bg-[#3397cf] rounded-lg transition-all duration-200 mt-2 hover:bg-opacity-90 active:scale-95 disabled:bg-gray-400 disabled:cursor-not-allowed focus:outline-none focus:ring-3 focus:ring-[#3397cf]/30"
          :disabled="isLoading || !isValid"
        >
          <span v-if="!isLoading">Register</span>
          <span v-else class="flex items-center justify-center gap-2">
            <svg class="w-5 h-5 animate-spin" viewBox="0 0 24 24">
              <circle
                class="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                stroke-width="3"
                fill="none"
              />
              <path
                class="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              />
            </svg>
            Registering...
          </span>
        </button>

        <!-- Login Link -->
        <div class="text-center text-sm text-gray-600 mt-6">
          <span>Already have an account? </span>
          <router-link
            to="/login"
            class="font-semibold text-[#3397cf] hover:text-opacity-80 transition-colors duration-200"
          >
            Login
          </router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>

</style>