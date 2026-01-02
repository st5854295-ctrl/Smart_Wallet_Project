package com.example.smartwallet;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PdfGenerator {

    // --- PDF Page Event Helper for Header and Footer ---
    private static class ReportHeaderFooter extends PdfPageEventHelper {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.WHITE);
        Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            // --- Header ---
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            headerTable.setSpacingAfter(20f);
            
            PdfPCell cell = new PdfPCell(new Phrase("SMART WALLET - FINANCIAL STATEMENT", headerFont));
            cell.setBackgroundColor(new BaseColor(63, 81, 181)); // Blue Header
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(10f);
            cell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(cell);
            
            try {
                document.add(headerTable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            // --- Footer ---
            String text = String.format("Page %d | This is a system-generated report for Smart Wallet.", writer.getPageNumber());
            Paragraph footer = new Paragraph(text, footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);

            PdfPTable footerTable = new PdfPTable(1);
            footerTable.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
            PdfPCell cell = new PdfPCell(footer);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            footerTable.addCell(cell);
            
            // Write footer at the bottom of the page
            footerTable.writeSelectedRows(0, -1, document.leftMargin(), document.bottom() - 10, writer.getDirectContent());
        }
    }

    public static void generateExpenseReport(Context context, List<Expense> expenseList, double totalBalance, String currencySymbol) {
        Toast.makeText(context, "Generating Professional PDF Report...", Toast.LENGTH_SHORT).show();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "SmartWallet_Report_" + timeStamp + ".pdf";

        try {
            OutputStream outputStream;
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10+ (API 29+): Use MediaStore
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                if (uri == null) {
                    throw new Exception("MediaStore URI was null");
                }
                outputStream = context.getContentResolver().openOutputStream(uri);
            } else {
                // For Android 9 and below: Use direct file path
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs();
                }
                File file = new File(downloadsDir, fileName);
                outputStream = new FileOutputStream(file);
                
                // Scan the file so it shows up in the gallery/downloads app immediately
                MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);
            }

            if (outputStream != null) {
                Document document = new Document(PageSize.A4);
                PdfWriter writer = PdfWriter.getInstance(document, outputStream);
                
                // Attach our custom header/footer event
                writer.setPageEvent(new ReportHeaderFooter());

                document.open();

                // --- Report Metadata ---
                Font metaFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
                document.add(new Paragraph("Report ID: " + UUID.randomUUID().toString().substring(0, 13).toUpperCase(), metaFont));
                document.add(new Paragraph("Statement Period: " + new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date()), metaFont));
                document.add(new Paragraph(" "));

                // --- Main Content Table ---
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{2, 4, 3, 2}); // Relative column widths

                // --- Table Header ---
                Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
                String[] headers = {"Date", "Description", "Category", "Amount"};
                for (String header : headers) {
                    PdfPCell headerCell = new PdfPCell(new Phrase(header, tableHeaderFont));
                    headerCell.setBackgroundColor(new BaseColor(63, 81, 181)); // Blue header match app theme
                    headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    headerCell.setPadding(8f);
                    headerCell.setBorderWidthBottom(2f); // Thick bottom border
                    headerCell.setBorderColorBottom(BaseColor.BLACK);
                    table.addCell(headerCell);
                }

                // --- Table Body ---
                Font boldRowFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
                Font defaultRowFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
                
                // Color coding
                BaseColor incomeColor = new BaseColor(232, 245, 233); // Light Green
                BaseColor expenseColor = new BaseColor(255, 235, 238); // Light Red
                BaseColor alternateColor = new BaseColor(245, 245, 245); // Light Gray for alternating rows

                int index = 0;
                for (Expense expense : expenseList) {
                    boolean isIncome = "Income".equals(expense.getType());
                    boolean isLargeTransaction = expense.getAmount() > 1000;
                    Font currentFont = isLargeTransaction ? boldRowFont : defaultRowFont;
                    
                    // Zebra Striping Logic & Income/Expense Highlight
                    BaseColor rowColor;
                    if (isIncome) {
                        rowColor = incomeColor;
                    } else {
                        rowColor = expenseColor; 
                    }
                    
                    // Override with zebra striping if needed, or keep color coding as priority
                    // Let's prioritize color coding as requested "Light green for income, light red for expense"
                    // If neither (maybe unknown type), use zebra
                    if (!isIncome && !"Expense".equals(expense.getType())) {
                         rowColor = (index % 2 == 0) ? BaseColor.WHITE : alternateColor;
                    }

                    // Date Cell (Center Aligned)
                    PdfPCell dateCell = new PdfPCell(new Phrase(expense.getDate(), currentFont));
                    dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    dateCell.setPadding(6f);
                    dateCell.setBackgroundColor(rowColor);
                    table.addCell(dateCell);

                    // Title and Category Cells
                    PdfPCell titleCell = new PdfPCell(new Phrase(expense.getTitle(), currentFont));
                    titleCell.setBackgroundColor(rowColor);
                    table.addCell(titleCell);
                    
                    PdfPCell catCell = new PdfPCell(new Phrase(expense.getCategory(), currentFont));
                    catCell.setBackgroundColor(rowColor);
                    table.addCell(catCell);

                    // Amount Cell (Right Aligned)
                    String sign = isIncome ? "+ " : "- ";
                    BaseColor amountTextColor = isIncome ? new BaseColor(38, 166, 154) : new BaseColor(239, 83, 80); // Green/Red text
                    Font amountFont = new Font(Font.FontFamily.HELVETICA, 10, isLargeTransaction ? Font.BOLD : Font.NORMAL, amountTextColor);
                    
                    PdfPCell amountCell = new PdfPCell(new Phrase(sign + currencySymbol + String.format(Locale.US, "%.2f", expense.getAmount()), amountFont));
                    amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    amountCell.setPadding(6f);
                    amountCell.setBackgroundColor(rowColor);
                    table.addCell(amountCell);
                    
                    index++;
                }

                document.add(table);

                // --- Total Balance Section ---
                document.add(new Paragraph(" "));
                
                PdfPTable summaryTable = new PdfPTable(2);
                summaryTable.setWidthPercentage(40);
                summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
                
                Font summaryFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
                
                PdfPCell summaryLabel = new PdfPCell(new Phrase("Total Balance:", summaryFont));
                summaryLabel.setBorder(Rectangle.TOP);
                summaryLabel.setPadding(5f);
                
                PdfPCell summaryValue = new PdfPCell(new Phrase(currencySymbol + String.format(Locale.US, "%.2f", totalBalance), summaryFont));
                summaryValue.setBorder(Rectangle.TOP);
                summaryValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
                summaryValue.setPadding(5f);
                
                summaryTable.addCell(summaryLabel);
                summaryTable.addCell(summaryValue);
                
                document.add(summaryTable);

                document.close();
                outputStream.close();
                
                Toast.makeText(context, "Professional PDF report saved to Downloads folder!", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(context, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}