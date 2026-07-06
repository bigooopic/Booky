package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.util.Base64
import com.example.data.Book
import com.example.data.BookPageConverter
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportBookToPdf(
        context: Context,
        book: Book,
        fontSizeSp: Int
    ): File? {
        val pdfDocument = PdfDocument()
        val pages = BookPageConverter.jsonToPages(book.pagesJson)

        // Standard A4 dimensions in PostScript points: 595 x 842
        val pageWidth = 595
        val pageHeight = 842

        val paint = Paint().apply {
            isAntiAlias = true
        }

        // --- PAGE 1: COVER PAGE ---
        val coverPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val coverPage = pdfDocument.startPage(coverPageInfo)
        val coverCanvas = coverPage.canvas

        // Background
        coverCanvas.drawColor(AndroidColor.parseColor("#FDFBF7"))

        // Border
        paint.color = AndroidColor.parseColor("#D32F2F") // Booky Red
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        coverCanvas.drawRect(25f, 25f, (pageWidth - 25).toFloat(), (pageHeight - 25).toFloat(), paint)

        // Decorative line
        paint.color = AndroidColor.parseColor("#FBC02D") // Booky Yellow
        paint.strokeWidth = 2f
        coverCanvas.drawRect(30f, 30f, (pageWidth - 30).toFloat(), (pageHeight - 30).toFloat(), paint)

        // Title
        paint.style = Paint.Style.FILL
        paint.color = AndroidColor.parseColor("#1C1C1E")
        paint.textSize = 36f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        coverCanvas.drawText(book.title, (pageWidth / 2).toFloat(), 250f, paint)

        // Author
        paint.textSize = 20f
        paint.color = AndroidColor.parseColor("#555555")
        paint.isFakeBoldText = false
        coverCanvas.drawText("By: ${book.author}", (pageWidth / 2).toFloat(), 320f, paint)

        // Decorative emblem line
        paint.color = AndroidColor.parseColor("#D32F2F")
        paint.strokeWidth = 3f
        coverCanvas.drawLine((pageWidth / 2 - 80).toFloat(), 380f, (pageWidth / 2 + 80).toFloat(), 380f, paint)

        // Booky watermark at bottom of cover
        paint.textSize = 14f
        paint.color = AndroidColor.parseColor("#8E8E93")
        coverCanvas.drawText("Created with Booky App", (pageWidth / 2).toFloat(), (pageHeight - 80).toFloat(), paint)

        pdfDocument.finishPage(coverPage)

        // --- CONTENT PAGES ---
        for (i in pages.indices) {
            val pageData = pages[i]
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i + 2).create()
            val pdfPage = pdfDocument.startPage(pageInfo)
            val canvas = pdfPage.canvas

            // Page Background
            canvas.drawColor(AndroidColor.parseColor("#FDFBF7"))

            // Thin frame border
            paint.style = Paint.Style.STROKE
            paint.color = AndroidColor.parseColor("#E5E5EA")
            paint.strokeWidth = 1f
            canvas.drawRect(30f, 30f, (pageWidth - 30).toFloat(), (pageHeight - 30).toFloat(), paint)

            paint.style = Paint.Style.FILL
            paint.color = AndroidColor.parseColor("#1C1C1E")
            paint.textAlign = Paint.Align.LEFT

            var yPosition = 70f

            // 1. Draw image if any
            if (!pageData.imageData.isNullOrEmpty()) {
                val bitmap = decodeBase64ToBitmap(pageData.imageData)
                if (bitmap != null) {
                    val maxWidth = pageWidth - 100
                    val maxHeight = 280
                    val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                    var destWidth = maxWidth
                    var destHeight = (maxWidth / ratio).toInt()
                    if (destHeight > maxHeight) {
                        destHeight = maxHeight
                        destWidth = (maxHeight * ratio).toInt()
                    }

                    val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
                    val destX = (pageWidth - destWidth) / 2
                    val destRect = Rect(destX, yPosition.toInt(), destX + destWidth, (yPosition + destHeight).toInt())
                    canvas.drawBitmap(bitmap, srcRect, destRect, paint)

                    yPosition += destHeight + 35f
                }
            }

            // 2. Draw Text (with automatic word wrap)
            val textPaint = Paint().apply {
                isAntiAlias = true
                textSize = fontSizeSp.toFloat()
                color = AndroidColor.parseColor("#1C1C1E")
                textAlign = Paint.Align.LEFT
            }

            val margin = 50f
            val maxTextWidth = pageWidth - 2 * margin
            val words = pageData.text.split(" ", "\n")
            var line = ""
            val lines = mutableListOf<String>()

            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                val width = textPaint.measureText(testLine)
                if (width > maxTextWidth) {
                    lines.add(line)
                    line = word
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) {
                lines.add(line)
            }

            // Render each wrapped line
            for (textLine in lines) {
                canvas.drawText(textLine, margin, yPosition, textPaint)
                yPosition += textPaint.textSize + 8f
                if (yPosition > pageHeight - 70) {
                    // Prevent page overflow
                    break
                }
            }

            // 3. Draw Page Number at bottom center
            val numPaint = Paint().apply {
                isAntiAlias = true
                textSize = 12f
                color = AndroidColor.parseColor("#8E8E93")
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("${i + 1}", (pageWidth / 2).toFloat(), (pageHeight - 45).toFloat(), numPaint)

            pdfDocument.finishPage(pdfPage)
        }

        // Save PDF file in cache
        val pdfFile = File(context.cacheDir, "Booky_${book.title.replace(" ", "_")}.pdf")
        return try {
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
