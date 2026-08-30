<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayWide=false>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${msg("loginTitle",(realm.displayName)!"")}</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
        }
    </style>
</head>
<body class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
        <div class="flex justify-center">
            <div class="w-16 h-16 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-2xl flex items-center justify-center shadow-lg">
                <span class="text-white text-2xl font-bold">A</span>
            </div>
        </div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
            ${(realm.displayName)!"ArchCore"}
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
            Sign in to your account
        </p>
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div class="bg-white py-8 px-4 shadow-xl sm:rounded-xl sm:px-10">
            <#if displayMessage?has_content && displayMessage>
                <#if message?has_content>
                    <div class="mb-4 p-4 rounded-lg <#if message.type == 'error'>bg-red-50 text-red-700<#elseif message.type == 'warning'>bg-yellow-50 text-yellow-700<#else>bg-green-50 text-green-700</#if>">
                        <p class="text-sm">${message.summary}</p>
                    </div>
                </#if>
            </#if>

            <#nested>

            <#if displayInfo?has_content && displayInfo>
                <div class="mt-6">
                    <#nested section="info">
                </div>
            </#if>
        </div>

        <p class="mt-8 text-center text-xs text-gray-500">
            Powered by <a href="https://archcore.io" class="font-medium text-indigo-600 hover:text-indigo-500">ArchCore</a>
        </p>
    </div>
</body>
</html>
</#macro>
