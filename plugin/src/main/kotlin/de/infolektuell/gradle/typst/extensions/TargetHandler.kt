package de.infolektuell.gradle.typst.extensions

import org.gradle.api.Named
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import java.io.Serializable
import javax.inject.Inject

sealed class TargetHandler(val extension: String) : Named {
    abstract val compileTaskName: Property<String>
}

abstract class PdfTargetHandler @Inject constructor() : TargetHandler("pdf") {
    enum class PdfStandard(val value: String) : Serializable {
        PDF_1_7("1.7"), PDF_A_2B("a-2b"), PDF_A_3B("a-3b")
    }

    abstract val outFile: RegularFileProperty
    abstract val pdfStandard: Property<PdfStandard>
}

abstract class PngTargetHandler : TargetHandler("png") {
    abstract val outDir: DirectoryProperty
    abstract val filenameTemplate: Property<String>
    abstract val ppi: Property<Int>
}

abstract class SvgTargetHandler : TargetHandler("svg") {
    abstract val outDir: DirectoryProperty
    abstract val filenameTemplate: Property<String>
}
