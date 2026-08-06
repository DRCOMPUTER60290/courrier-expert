package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.LetterEntity
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    fun generatePdf(context: Context, letter: LetterEntity): File? {
        try {
            val pdfDocument = PdfDocument()
            // Standard A4 dimensions in points (595 x 842)
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint().apply {
                isAntiAlias = true
                textSize = 11f
                color = Color.BLACK
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }

            val boldPaint = Paint().apply {
                isAntiAlias = true
                textSize = 11f
                color = Color.BLACK
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }

            val titlePaint = Paint().apply {
                isAntiAlias = true
                textSize = 12f
                color = Color.rgb(30, 58, 138) // Navy blue
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }

            var currentY = 50f
            val leftMargin = 50f
            val rightMargin = 545f
            val contentWidth = rightMargin - leftMargin

            // 1. Expéditeur (Top-Left)
            canvas.drawText(letter.senderName.ifEmpty { "[Nom & Prénom]" }, leftMargin, currentY, boldPaint)
            currentY += 15f
            if (letter.senderAddress.isNotEmpty()) {
                canvas.drawText(letter.senderAddress, leftMargin, currentY, paint)
                currentY += 14f
            }
            val zipCity = "${letter.senderZipCode} ${letter.senderCity}".trim()
            if (zipCity.isNotEmpty()) {
                canvas.drawText(zipCity, leftMargin, currentY, paint)
                currentY += 14f
            }
            if (letter.senderPhone.isNotEmpty()) {
                canvas.drawText("Tél : ${letter.senderPhone}", leftMargin, currentY, paint)
                currentY += 14f
            }
            if (letter.senderEmail.isNotEmpty()) {
                canvas.drawText("Email : ${letter.senderEmail}", leftMargin, currentY, paint)
                currentY += 14f
            }

            // 2. Destinataire (Top-Right Block)
            var recipientY = 70f
            val recipientX = 320f
            canvas.drawText(letter.recipientName.ifEmpty { "[Organisme / Destinataire]" }, recipientX, recipientY, boldPaint)
            recipientY += 15f
            if (letter.recipientAddress.isNotEmpty()) {
                canvas.drawText(letter.recipientAddress, recipientX, recipientY, paint)
                recipientY += 14f
            }
            val recZipCity = "${letter.recipientZipCode} ${letter.recipientCity}".trim()
            if (recZipCity.isNotEmpty()) {
                canvas.drawText(recZipCity, recipientX, recipientY, paint)
                recipientY += 14f
            }

            currentY = maxOf(currentY + 20f, recipientY + 30f)

            // 3. Fait à ..., le ...
            val cityDateText = letter.cityDate.ifEmpty { "Fait le ..." }
            canvas.drawText(cityDateText, recipientX, currentY, paint)
            currentY += 40f

            // 4. Objet
            canvas.drawText("Objet : ${letter.subject}", leftMargin, currentY, titlePaint)
            currentY += 20f

            // 5. Références / PJ (if any)
            if (letter.referencesText.isNotEmpty()) {
                canvas.drawText("Réf. : ${letter.referencesText}", leftMargin, currentY, paint)
                currentY += 20f
            }

            currentY += 10f

            // 6. Body Text with multiline wrapping
            val paragraphs = letter.body.split("\n")
            for (paragraph in paragraphs) {
                if (paragraph.isBlank()) {
                    currentY += 10f
                    continue
                }
                val lines = wrapText(paragraph, paint, contentWidth)
                for (line in lines) {
                    if (currentY > 780f) break // Simple 1-page bounds
                    canvas.drawText(line, leftMargin, currentY, paint)
                    currentY += 16f
                }
                currentY += 6f
            }

            currentY += 15f

            // 7. Formule de politesse
            if (letter.politeForm.isNotEmpty() && currentY < 760f) {
                val politeLines = wrapText(letter.politeForm, paint, contentWidth)
                for (line in politeLines) {
                    canvas.drawText(line, leftMargin, currentY, paint)
                    currentY += 16f
                }
            }

            currentY += 30f

            // 8. Signature Block (Bottom Right)
            if (currentY < 790f) {
                canvas.drawText("Signature :", recipientX, currentY, boldPaint)
                currentY += 15f
                canvas.drawText(letter.senderName.ifEmpty { "" }, recipientX, currentY, paint)
            }

            pdfDocument.finishPage(page)

            val pdfFile = File(context.cacheDir, "Courrier_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
            val width = paint.measureText(testLine)
            if (width <= maxWidth) {
                currentLine.append(if (currentLine.isEmpty()) word else " $word")
            } else {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    fun sharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Partager le courrier (PDF)"))
    }

    fun sendEmailWithPdf(context: Context, pdfFile: File, subject: String = "", recipientEmail: String = "") {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            if (recipientEmail.isNotEmpty()) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
            }
            putExtra(Intent.EXTRA_SUBJECT, subject.ifEmpty { "Courrier officiel PDF" })
            putExtra(Intent.EXTRA_TEXT, "Veuillez trouver ci-joint le courrier au format PDF.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(emailIntent, "Envoyer le courrier par e-mail"))
    }
}
