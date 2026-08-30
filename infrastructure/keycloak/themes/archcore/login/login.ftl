<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        ${msg("loginTitle",(realm.displayName)!"")}
    <#elseif section = "header">
        <#if realm.displayName?has_content>
            ${msg("loginTitleHtml",(realm.displayName?html)!"")}
        </#if>
    <#elseif section = "form">
        <#if realm.passwordAllowed>
            <div id="kc-form">
                <div id="kc-form-wrapper">
                    <#if realm.socialProviders?has_content && realm.socialProviders.loginProviders?has_content>
                        <div id="kc-social-providers" class="mb-6">
                            <#list realm.socialProviders.loginProviders as p>
                                <a href="${p.loginUrl}" id="social-${p.alias}" class="flex items-center justify-center w-full px-4 py-3 mb-3 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
                                    <#if p.iconUrl?has_content>
                                        <img src="${p.iconUrl}" alt="${p.displayName}" class="w-5 h-5 mr-3">
                                    </#if>
                                    ${p.displayName}
                                </a>
                            </#list>
                        </div>
                        <div class="relative mb-6">
                            <div class="absolute inset-0 flex items-center">
                                <div class="w-full border-t border-gray-200"></div>
                            </div>
                            <div class="relative flex justify-center text-sm">
                                <span class="px-4 bg-white text-gray-500">or continue with email</span>
                            </div>
                        </div>
                    </#if>

                    <form id="kc-form-login" onsubmit="return true;" action="${url.loginAction}" method="post">
                        <div class="mb-4">
                            <label for="username" class="block text-sm font-medium text-gray-700 mb-2">
                                <#if !realm.loginWithEmailAllowed>
                                    ${msg("username")}
                                <#elseif !realm.registrationEmailAsUsername>
                                    ${msg("usernameOrEmail")}
                                <#else>
                                    ${msg("email")}
                                </#if>
                            </label>
                            <input
                                id="username"
                                name="username"
                                value="${(login.username)!''}"
                                type="text"
                                autofocus
                                autocomplete="username"
                                aria-invalid="<#if messages.exists('usernameError')>true</#if>"
                                class="w-full px-4 py-3 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors <#if messages.exists('usernameError')>border-red-500</#if>"
                                placeholder="Enter your email"
                            />
                            <#if messages.exists('usernameError')>
                                <p class="mt-2 text-sm text-red-600">${msg("usernameError")}</p>
                            </#if>
                        </div>

                        <div class="mb-6">
                            <label for="password" class="block text-sm font-medium text-gray-700 mb-2">
                                ${msg("password")}
                            </label>
                            <input
                                id="password"
                                name="password"
                                type="password"
                                autocomplete="current-password"
                                aria-invalid="<#if messages.exists('passwordError')>true</#if>"
                                class="w-full px-4 py-3 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors <#if messages.exists('passwordError')>border-red-500</#if>"
                                placeholder="Enter your password"
                            />
                            <#if messages.exists('passwordError')>
                                <p class="mt-2 text-sm text-red-600">${msg("passwordError")}</p>
                            </#if>
                        </div>

                        <div class="flex items-center justify-between mb-6">
                            <#if realm.rememberMeAllowed?has_content && realm.rememberMeAllowed>
                                <label class="flex items-center">
                                    <input
                                        id="rememberMe"
                                        name="rememberMe"
                                        type="checkbox"
                                        <#if login.rememberMe?has_content && login.rememberMe>checked</#if>
                                        class="w-4 h-4 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500"
                                    />
                                    <span class="ml-2 text-sm text-gray-600">${msg("rememberMe")}</span>
                                </label>
                            <#else>
                                <div></div>
                            </#if>
                            <#if realm.resetPasswordAllowed>
                                <a href="${url.loginResetCredentialsUrl}" class="text-sm font-medium text-indigo-600 hover:text-indigo-500 transition-colors">
                                    ${msg("doForgotPassword")}
                                </a>
                            </#if>
                        </div>

                        <input type="hidden" id="loginContextSetupData" name="loginContextSetupData" value="${(loginContextSetupData)!''}" />

                        <button
                            type="submit"
                            name="login"
                            id="kc-login"
                            class="w-full px-4 py-3 text-sm font-semibold text-white bg-gray-900 rounded-lg hover:bg-gray-800 focus:ring-4 focus:ring-gray-300 transition-all"
                        >
                            ${msg("doLogIn")}
                        </button>
                    </form>

                    <#if realm.registrationAllowed && !realm.registrationEmailAsUsername?has_content>
                        <div class="mt-6 text-center">
                            <p class="text-sm text-gray-600">
                                ${msg("noAccount")}
                                <a href="${url.registrationUrl}" class="font-medium text-indigo-600 hover:text-indigo-500 transition-colors">
                                    ${msg("doRegister")}
                                </a>
                            </p>
                        </div>
                    </#if>
                </div>
            </div>
        </#if>
    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !realm.registrationEmailAsUsername?has_content>
            <div id="kc-registration">
                <span class="text-sm text-gray-600">
                    ${msg("noAccount")}
                    <a href="${url.registrationUrl}" class="font-medium text-indigo-600 hover:text-indigo-500 transition-colors">
                        ${msg("doRegister")}
                    </a>
                </span>
            </div>
        </#if>
    </#if>
</@layout.registrationLayout>
