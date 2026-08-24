package com.company.exportplatform.service;

import com.company.exportplatform.dto.response.ReceiptResponse;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Renders a payment receipt (REC) to PDF: received-from block, payment
 * breakdown with gateway reference, amount in words and remaining balance.
 */
@Service
public class ReceiptPdfService {

    private static final Color NAVY = new Color(15, 23, 42);
    private static final Color GOLD = new Color(176, 141, 20);
    private static final Color LIGHT_ROW = new Color(244, 246, 250);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final String[] ONES = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen",
            "Eighteen", "Nineteen"
    };
    private static final String[] TENS = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public byte[] render(ReceiptResponse r) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, r);
            document.add(receivedFromBlock(r));
            document.add(paymentTable(r));
            document.add(wordsBlock(r));
            if (notBlank(r.notes())) {
                document.add(spacer(8));
                document.add(new Paragraph("NOTES", bold(9, Color.GRAY)));
                document.add(new Paragraph(r.notes(), small(Font.NORMAL, Color.DARK_GRAY)));
            }

            Paragraph footer = new Paragraph(
                    "This is a computer-generated payment receipt. For queries quote the receipt number above.",
                    small(Font.NORMAL, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(16);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Could not render receipt PDF: " + ex.getMessage(), ex);
        }
    }

    private void addHeader(Document document, ReceiptResponse r) throws Exception {
        PdfPTable header = new PdfPTable(new float[]{1f, 1f});
        header.setWidthPercentage(100);

        PdfPCell brand = cell();
        brand.addElement(new Paragraph("GLOBAL EXPORT PVT. LTD.", heading(15, NAVY)));
        brand.addElement(new Paragraph("D-14 Industrial Estate, Mumbai 400001, India", small(Font.NORMAL, Color.DARK_GRAY)));
        brand.addElement(new Paragraph("GSTIN: 27ABCDE1234F1Z5   PAN: ABCDE1234F", tiny(Font.NORMAL, Color.GRAY)));
        brand.addElement(new Paragraph("+91 22 4000 1234  •  accounts@globalexport.example", tiny(Font.NORMAL, Color.GRAY)));
        header.addCell(brand);

        PdfPCell meta = cell();
        meta.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph title = new Paragraph("PAYMENT RECEIPT", heading(15, GOLD));
        title.setAlignment(Element.ALIGN_RIGHT);
        meta.addElement(title);
        meta.addElement(new Paragraph(r.receiptNo(), bold(11, NAVY)));
        if (r.issuedOn() != null) {
            rightLine(meta, "Issued on: " + DATE.format(r.issuedOn()));
        }
        header.addCell(meta);

        document.add(header);
        document.add(spacer(10));
    }

    private Element receivedFromBlock(ReceiptResponse r) {
        PdfPTable table = new PdfPTable(new float[]{1.2f, 0.8f});
        table.setWidthPercentage(100);

        PdfPCell from = cell();
        from.addElement(new Paragraph("RECEIVED FROM", bold(9, Color.GRAY)));
        boolean hasCompany = notBlank(r.clientCompanyName());
        from.addElement(new Paragraph(hasCompany ? r.clientCompanyName()
                : (notBlank(r.clientName()) ? r.clientName() : "Client"), bold(11, NAVY)));
        if (hasCompany && notBlank(r.clientName())) {
            from.addElement(new Paragraph("Attn: " + r.clientName(), small(Font.NORMAL, Color.DARK_GRAY)));
        }
        if (notBlank(methodLabel(r))) {
            from.addElement(new Paragraph("Paid via " + methodLabel(r), small(Font.NORMAL, Color.DARK_GRAY)));
        }

        PdfPCell refs = cell();
        refs.setHorizontalAlignment(Element.ALIGN_RIGHT);
        refs.addElement(new Paragraph("REFERENCES", bold(9, Color.GRAY)));
        if (notBlank(r.invoiceNo())) {
            refs.addElement(new Paragraph("Tax Invoice: " + r.invoiceNo(), small(Font.NORMAL, Color.DARK_GRAY)));
        }
        if (notBlank(r.piNo())) {
            refs.addElement(new Paragraph("Proforma: " + r.piNo(), small(Font.NORMAL, Color.DARK_GRAY)));
        }

        table.addCell(from);
        table.addCell(refs);
        return table;
    }

    private Element paymentTable(ReceiptResponse r) {
        PdfPTable table = new PdfPTable(new float[]{1f});
        table.setWidthPercentage(70);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setSpacingBefore(14);

        row(table, "Amount received", money(r.amount(), r.currency()), true);
        row(table, "Payment method", methodLabel(r), false);
        if (notBlank(r.gatewayTransactionId())) {
            row(table, "Transaction ref", r.gatewayTransactionId(), false);
        }
        row(table, "Balance remaining on invoice",
                money(nvl(r.remainingBalance()), r.currency()), false);
        return table;
    }

    private Element wordsBlock(ReceiptResponse r) throws Exception {
        PdfPTable table = new PdfPTable(new float[]{1f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(12);
        PdfPCell cell = new PdfPCell(new Phrase(
                "Amount in words: " + r.currency() + " " + amountInWords(r.amount()) + " only",
                small(Font.ITALIC, Color.DARK_GRAY)));
        cell.setBackgroundColor(LIGHT_ROW);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        table.addCell(cell);
        return table;
    }

    // ---------- low-level helpers ----------

    private String methodLabel(ReceiptResponse r) {
        return r.method() != null ? r.method().name().replace('_', ' ') : "-";
    }

    private void row(PdfPTable table, String label, String value, boolean emphasis) {
        Font font = emphasis ? bold(10, NAVY) : small(Font.NORMAL, Color.DARK_GRAY);
        int borders = emphasis ? Rectangle.TOP | Rectangle.BOTTOM : Rectangle.NO_BORDER;
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(borders);
        labelCell.setBorderWidthTop(emphasis ? 1f : 0f);
        labelCell.setBackgroundColor(emphasis ? LIGHT_ROW : Color.WHITE);
        labelCell.setPaddingTop(4);
        labelCell.setPaddingBottom(4);
        table.addCell(labelCell);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(borders);
        valueCell.setBorderWidthTop(emphasis ? 1f : 0f);
        valueCell.setBackgroundColor(emphasis ? LIGHT_ROW : Color.WHITE);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPaddingTop(4);
        valueCell.setPaddingBottom(4);
        table.addCell(valueCell);
    }

    private PdfPCell cell() {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private void rightLine(PdfPCell cell, String text) {
        Paragraph p = new Paragraph(text, small(Font.NORMAL, Color.DARK_GRAY));
        p.setAlignment(Element.ALIGN_RIGHT);
        cell.addElement(p);
    }

    private static Paragraph spacer(float points) {
        Paragraph p = new Paragraph(" ");
        p.setLeading(points);
        return p;
    }

    private Font heading(int size, Color color) {
        return new Font(Font.HELVETICA, size, Font.BOLD, color);
    }

    private Font bold(int size, Color color) {
        return new Font(Font.HELVETICA, size, Font.BOLD, color);
    }

    private Font small(int style, Color color) {
        return new Font(Font.HELVETICA, 9, style, color);
    }

    private Font tiny(int style, Color color) {
        return new Font(Font.HELVETICA, 7.5f, style, color);
    }

    private String money(BigDecimal amount, String currency) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        return currency + " " + String.format(Locale.ENGLISH, "%,.2f", amount);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    /** Indian-format words: crore / lakh / thousand. Handles up to 99 crore. */
    static String amountInWords(BigDecimal amount) {
        BigDecimal safe = amount == null ? BigDecimal.ZERO : amount;
        long whole = safe.setScale(0, RoundingMode.DOWN).longValueExact();
        int paise = safe.remainder(BigDecimal.ONE).movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP).intValueExact();
        if (whole == 0 && paise == 0) {
            return "Zero";
        }
        StringBuilder sb = new StringBuilder();
        long crore = whole / 10_000_000;
        long lakh = (whole / 100_000) % 100;
        long thousand = (whole / 1_000) % 100;
        long hundreds = (whole / 100) % 10;
        long rest = whole % 100;
        if (crore > 0) sb.append(twoDigits(crore)).append(" Crore ");
        if (lakh > 0) sb.append(twoDigits(lakh)).append(" Lakh ");
        if (thousand > 0) sb.append(twoDigits(thousand)).append(" Thousand ");
        if (hundreds > 0) sb.append(ONES[(int) hundreds]).append(" Hundred ");
        if (rest > 0) {
            if (sb.length() > 0) sb.append("and ");
            sb.append(twoDigits(rest));
        }
        if (paise > 0) {
            if (sb.length() == 0) sb.append("Zero");
            sb.append(" Point ").append(twoDigits(paise)).append(" Paise");
        }
        return sb.toString().trim();
    }

    private static String twoDigits(long n) {
        if (n < 20) {
            return ONES[(int) n];
        }
        int t = (int) (n / 10);
        int o = (int) (n % 10);
        return TENS[t] + (o > 0 ? " " + ONES[o] : "");
    }
}
