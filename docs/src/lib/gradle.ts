import pkg from '../../package.json'
import { z } from 'astro:content'
import { XMLParser } from 'fast-xml-parser'

export const mavenSchema = z.object({
    metadata: z.object({
        version: z.string(),
    }),
})

type GradlePlugin = {
    id: string,
    displayName: string,
    version: string,
    href: string
}

export const createPlugin = async function (id: string): Promise<GradlePlugin> {
    const href = 'https://plugins.gradle.org/plugin/' + id
    const { displayName } = pkg.gradle

    const pluginMetadata = `https://plugins.gradle.org/m2/${pkg.gradle.group.replaceAll('.', '/')}/${pkg.gradle.artifact}/maven-metadata.xml`
    const response = await fetch(pluginMetadata)
    if (!response.ok) {
        const version = 'x.y.z'
        return { id, displayName, version, href }
    }
    const xml = await response.text()
    const parser = new XMLParser()
    const json = parser.parse(xml)
    const data = mavenSchema.parse(json)
    const { version } = data.metadata
    return { id, version, displayName, href }
}

const plugin = await createPlugin(pkg.gradle.id)
export default plugin
