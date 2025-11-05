// @ts-check
import { defineConfig } from 'astro/config'
import starlight from '@astrojs/starlight'
import starlightAutoImport from './src/plugins/starlight-auto-import'

// https://astro.build/config
export default defineConfig({
    site: 'https://infolektuell.github.io',
    base: '/gradle-typst/',
    trailingSlash: 'always',
    integrations: [
        starlight({
            plugins: [starlightAutoImport()],
            title: 'Gradle Typst Plugin',
            description: 'Generates PDF documents using the Typst markup system',
            logo: {
                src: './src/assets/logo.svg',
            },
            social: [
                {
                    icon: 'seti:gradle',
                    label: 'Gradle Plugin Portal',
                    href: 'https://plugins.gradle.org/plugin/de.infolektuell.typst',
                },
                { icon: 'github', label: 'GitHub', href: 'https://github.com/infolektuell/gradle-typst' },
            ],
            editLink: {
                baseUrl: 'https://github.com/infolektuell/gradle-typst/edit/main/docs/',
            },
            components: {
                SiteTitle: './src/components/SiteTitle.astro',
            },
            sidebar: [
                {
                    label: 'Getting Started',
                    autogenerate: { directory: 'start' },
                },
                {
                    label: 'API Docs',
                    link: 'https://infolektuell.github.io/gradle-typst/reference/',
                    attrs: { target: '_blank' },
                },
            ],
        }),
    ],
    devToolbar: {
        enabled: false,
    },
})
