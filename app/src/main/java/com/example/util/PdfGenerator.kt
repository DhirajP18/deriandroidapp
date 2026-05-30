package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.api.BillReportData
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generateBillPdf(context: Context, data: BillReportData): File {
        val pdfDocument = PdfDocument()
        
        // Setup standard A4 size (approx 595 x 842 points)
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        
        val canvas: Canvas = page.canvas
        val paint = Paint()
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }
        
        var currentY = 50f
        
        // 1. Draw Title Header
        paint.color = Color.rgb(13, 148, 136) // Dark Teal DERISET color
        canvas.drawRect(30f, currentY, pageWidth - 30f, currentY + 45f, paint)
        
        textPaint.color = Color.WHITE
        textPaint.textSize = 18f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val titleText = data.dairyName ?: "DERISET DAIRY"
        val titleWidth = textPaint.measureText(titleText)
        canvas.drawText(titleText, (pageWidth - titleWidth) / 2f, currentY + 28f, textPaint)
        
        currentY += 65f
        
        // 2. Draw Sub-header Info (Farmer detail)
        textPaint.color = Color.BLACK
        textPaint.textSize = 11f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        
        canvas.drawText("Farmer: ${data.farmerName ?: "N/A"} (${data.farmerCode ?: "N/A"})", 35f, currentY, textPaint)
        canvas.drawText("Period: ${data.period ?: "N/A"}", 35f, currentY + 18f, textPaint)
        canvas.drawText("Bill Date: ${DateUtils.getTodayFormatted()}", pageWidth - 180f, currentY, textPaint)
        
        currentY += 40f
        
        // Draw elegant table header
        paint.color = Color.rgb(241, 245, 249) // Slate Background
        canvas.drawRect(30f, currentY, pageWidth - 30f, currentY + 25f, paint)
        
        paint.color = Color.rgb(13, 148, 136)
        paint.strokeWidth = 1f
        canvas.drawLine(30f, currentY, pageWidth - 30f, currentY, paint)
        canvas.drawLine(30f, currentY + 25f, pageWidth - 30f, currentY + 25f, paint)
        
        textPaint.color = Color.BLACK
        textPaint.textSize = 9f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        
        // Table columns headers
        canvas.drawText("Date", 35f, currentY + 16f, textPaint)
        canvas.drawText("M. Ltr", 110f, currentY + 16f, textPaint)
        canvas.drawText("M. Fat", 160f, currentY + 16f, textPaint)
        canvas.drawText("M. Rate", 210f, currentY + 16f, textPaint)
        canvas.drawText("M. Amt", 260f, currentY + 16f, textPaint)
        
        canvas.drawText("E. Ltr", 320f, currentY + 16f, textPaint)
        canvas.drawText("E. Fat", 370f, currentY + 16f, textPaint)
        canvas.drawText("E. Rate", 420f, currentY + 16f, textPaint)
        canvas.drawText("E. Amt", 470f, currentY + 16f, textPaint)
        
        currentY += 25f
        
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        
        // 3. Draw rows
        data.rows?.forEach { row ->
            // Prevent overflowing the first page (quick check, standard pagination can be expanded if needed)
            if (currentY + 20f > pageHeight - 120f) {
                // Limit rows or gracefully draw line (for typical 10-day summary, fits within single page)
            }
            
            val formattedDate = DateUtils.formatCompactDate(row.date)
            canvas.drawText(formattedDate, 35f, currentY + 14f, textPaint)
            
            canvas.drawText(String.format("%.1f", row.morningLitres ?: 0.0), 110f, currentY + 14f, textPaint)
            canvas.drawText(String.format("%.1f", row.morningFat ?: 0.0), 160f, currentY + 14f, textPaint)
            canvas.drawText(String.format("₹%.2f", row.morningRate ?: 0.0), 210f, currentY + 14f, textPaint)
            canvas.drawText(String.format("₹%.2f", row.morningAmount ?: 0.0), 260f, currentY + 14f, textPaint)
            
            canvas.drawText(String.format("%.1f", row.eveningLitres ?: 0.0), 320f, currentY + 14f, textPaint)
            canvas.drawText(String.format("%.1f", row.eveningFat ?: 0.0), 370f, currentY + 14f, textPaint)
            canvas.drawText(String.format("₹%.2f", row.eveningRate ?: 0.0), 420f, currentY + 14f, textPaint)
            canvas.drawText(String.format("₹%.2f", row.eveningAmount ?: 0.0), 470f, currentY + 14f, textPaint)
            
            // Draw standard separator line
            paint.color = Color.rgb(226, 232, 240)
            canvas.drawLine(30f, currentY + 20f, pageWidth - 30f, currentY + 20f, paint)
            
            currentY += 21f
        }
        
        currentY += 15f
        
        // 4. Summaries & Deductions
        paint.color = Color.rgb(13, 148, 136)
        canvas.drawLine(30f, currentY, pageWidth - 30f, currentY, paint)
        currentY += 15f
        
        textPaint.textSize = 10f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL DIRECT MILK DETAILS:", 35f, currentY, textPaint)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Total Litres Collected: ${String.format("%.2f Ltr", data.totalLitres ?: 0.0)}", 35f, currentY + 16f, textPaint)
        canvas.drawText("Total Milk Earnings: ${String.format("₹%.2f", data.totalAmount ?: 0.0)}", 35f, currentY + 32f, textPaint)
        
        currentY += 50f
        
        // Deductions list
        if (!data.deductions.isNullOrEmpty()) {
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("DEDUCTIONS APPLIED:", 35f, currentY, textPaint)
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            
            var deductionY = currentY + 16f
            data.deductions.forEach { dec ->
                canvas.drawText("- ${dec.type ?: "Deduction"}: ${String.format("₹%.2f", dec.amount ?: 0.0)} (${dec.remarks ?: ""})", 35f, deductionY, textPaint)
                deductionY += 14f
            }
            currentY = deductionY + 10f
        }
        
        // Net Payable Box
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRect(300f, currentY - 10f, pageWidth - 30f, currentY + 35f, paint)
        paint.color = Color.rgb(13, 148, 136)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawRect(300f, currentY - 10f, pageWidth - 30f, currentY + 35f, paint)
        paint.style = Paint.Style.FILL
        
        textPaint.color = Color.rgb(13, 148, 136)
        textPaint.textSize = 13f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NET PAYABLE AMOUNT:", 310f, currentY + 10f, textPaint)
        canvas.drawText(String.format("₹%.2f", data.netPayable ?: 0.0), 310f, currentY + 28f, textPaint)
        
        currentY += 65f
        
        // Footer Signature lines
        textPaint.color = Color.DKGRAY
        textPaint.textSize = 9f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Generated electronically via DERISET app", 35f, currentY, textPaint)
        canvas.drawText("Authorized Signature", pageWidth - 160f, currentY, textPaint)
        
        pdfDocument.finishPage(page)
        
        // Save PDF to cache or temporary folder
        val cacheFile = File(context.cacheDir, "DERISET_Bill_${data.farmerCode ?: "Report"}.pdf")
        FileOutputStream(cacheFile).use { fos ->
            pdfDocument.writeTo(fos)
        }
        pdfDocument.close()
        
        return cacheFile
    }
}
