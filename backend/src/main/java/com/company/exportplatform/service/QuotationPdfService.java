package com.company.exportplatform.service;

import com.company.exportplatform.dto.response.QuotationItemResponse;
import com.company.exportplatform.dto.response.QuotationResponse;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Renders a quotation to PDF with OpenPDF. All money formatting is plain
 * grouping; currency code is printed alongside amounts.
 */
@Service
public class QuotationPdfService {

    private static final Color NAVY = new Color(15, 23, 42);
    private static final Color GOLD = new Color(212, 160, 23);
    private static final Color LIGHT_ROW = new Color(244, 246, 250);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    public byte[] render(QuotationResponse q) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, q);
            document.add(partiesBlock(q));
            document.add(itemsTable(q));
            document.add(totalsTable(q));
            addTextSections(document, q);

            Paragraph footer = new Paragraph(
                    "This is a computer-generated quotation and is valid only in its original PDF form.",
                    small(Font.NORMAL, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(14);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Could not render quotation PDF: " + ex.getMessage(), ex);
        }
    }

    private void addHeader(Document document, QuotationResponse q) throws Exception {
        PdfPTable header = new PdfPTable(new float[]{1f, 1f});
        header.setWidthPercentage(100);

        PdfPCell brand = cell();
        brand.addElement(new Paragraph("GLOBAL EXPORT", heading(16, NAVY)));
        brand.addElement(new Paragraph("Cargo • Vessels • Worldwide Shipping", small(Font.NORMAL, Color.DARK_GRAY)));
        brand.addElement(new Paragraph("D-14 Industrial Estate, Mumbai 400001, India", tiny(Font.NORMAL, Color.GRAY)));
        brand.addElement(new Paragraph("+91 22 4000 1234  •  sales@globalexport.example", tiny(Font.NORMAL, Color.GRAY)));
        header.addCell(brand);

        PdfPCell meta = cell();
        meta.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph title = new Paragraph("QUOTATION", heading(18, GOLD));
        title.setAlignment(Element.ALIGN_RIGHT);
        meta.addElement(title);
        meta.addElement(new Paragraph(q.quoteNo(), bold(11, NAVY)));
        if (q.quotationDate() != null) {
            rightLine(meta, "Date: " + DATE.format(q.quotationDate()));
        }
        if (q.validUntil() != null) {
            rightLine(meta, "Valid until: " + DATE.format(q.validUntil()));
        }
        if (q.incoterms() != null && !q.incoterms().isBlank()) {
            rightLine(meta, "Incoterms: " + q.incoterms());
        }
        header.addCell(meta);

        document.add(header);
        document.add(spacer(10));
    }

    private Element partiesBlock(QuotationResponse q) {
        PdfPTable parties = new PdfPTable(new float[]{1.2f, 0.8f});
        parties.setWidthPercentage(100);

        PdfPCell billTo = cell();
        billTo.addElement(new Paragraph("BILL TO", bold(9, Color.GRAY)));
        String name = q.clientCompanyName() != null ? q.clientCompanyName() : "Client";
        billTo.addElement(new Paragraph(name, bold(11, NAVY)));
        List<String> addressLines = new ArrayList<>();
        if (notBlank(q.billingAddressLine1())) addressLines.add(q.billingAddressLine1());
        if (notBlank(q.billingAddressLine2())) addressLines.add(q.billingAddressLine2());
        StringBuilder cityLine = new StringBuilder();
        if (notBlank(q.billingCity())) cityLine.append(q.billingCity());
        if (notBlank(q.billingState())) cityLine.append(cityLine.length() > 0 ? ", " : "").append(q.billingState());
        if (notBlank(q.billingPostalCode())) cityLine.append(cityLine.length() > 0 ? " - " : "").append(q.billingPostalCode());
        if (cityLine.length() > 0) addressLines.add(cityLine.toString());
        if (notBlank(q.billingCountry())) addressLines.add(q.billingCountry());
        for (String line : addressLines) {
            billTo.addElement(new Paragraph(line, small(Font.NORMAL, Color.DARK_GRAY)));
        }
        if (notBlank(q.contactEmail())) billTo.addElement(new Paragraph(q.contactEmail(), small(Font.NORMAL, Color.DARK_GRAY)));
        if (notBlank(q.contactPhone())) billTo.addElement(new Paragraph(q.contactPhone(), small(Font.NORMAL, Color.DARK_GRAY)));
        parties.addCell(billTo);

        PdfPCell enquiry = cell();
        enquiry.setHorizontalAlignment(Element.ALIGN_RIGHT);
        enquiry.addElement(new Paragraph("REFERENCE", bold(9, Color.GRAY)));
        if (q.enquiryRef() != null) {
            enquiry.addElement(new Paragraph("Enquiry " + q.enquiryRef(), small(Font.NORMAL, Color.DARK_GRAY)));
        }
        enquiry.addElement(new Paragraph("Currency: " + q.currency(), small(Font.NORMAL, Color.DARK_GRAY)));
        parties.addCell(enquiry);

        return parties;
    }

    private Element itemsTable(QuotationResponse q) throws Exception {
        PdfPTable table = new PdfPTable(new float[]{0.5f, 4.4f, 1f, 1f, 1.3f, 1.3f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(12);

        table.addCell(headerCell("#"));
        table.addCell(headerCell("Description"));
        table.addCell(headerCell("Qty"));
        table.addCell(headerCell("Unit"));
        table.addCell(headerCell("Rate"));
        table.addCell(headerCell("Amount"));

        int i = 1;
        boolean stripe = false;
        for (QuotationItemResponse item : q.items()) {
            Color bg = stripe ? LIGHT_ROW : Color.WHITE;
            stripe = !stripe;
            table.addCell(bodyCell(String.valueOf(i++), bg, Element.ALIGN_CENTER));
            table.addCell(bodyCell(nullSafe(item.description()), bg, Element.ALIGN_LEFT));
            table.addCell(bodyCell(strip(item.quantity()), bg, Element.ALIGN_CENTER));
            table.addCell(bodyCell(nullSafe(item.unit()), bg, Element.ALIGN_CENTER));
            table.addCell(bodyCell(money(item.ratePerUnit(), q.currency()), bg, Element.ALIGN_RIGHT));
            table.addCell(bodyCell(money(item.lineAmount(), q.currency()), bg, Element.ALIGN_RIGHT));
        }
        return table;
    }

    private Element totalsTable(QuotationResponse q) {
        PdfPTable totals = new PdfPTable(new float[]{1f});
        totals.setWidthPercentage(58);
        totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totals.setSpacingBefore(10);

        row(totals, "Subtotal", money(q.subtotal(), q.currency()), false);
        if (gtZero(q.discount())) row(totals, "Discount", "-" + money(q.discount(), q.currency()), false);
        if (gtZero(q.freightCharges())) row(totals, "Freight", money(q.freightCharges(), q.currency()), false);
        if (gtZero(q.loadingCharges())) row(totals, "Loading charges", money(q.loadingCharges(), q.currency()), false);
        if (gtZero(q.documentationCharges())) row(totals, "Documentation", money(q.documentationCharges(), q.currency()), false);
        if (gtZero(q.insuranceCharges())) row(totals, "Insurance", money(q.insuranceCharges(), q.currency()), false);
        if (gtZero(q.otherCharges())) row(totals, "Other charges", money(q.otherCharges(), q.currency()), false);
        row(totals, "Taxable amount", money(q.taxableAmount(), q.currency()), false);

        boolean cgstSplit = gtZero(q.cgstAmount()) || gtZero(q.sgstAmount());
        if (cgstSplit) {
            if (q.taxRateName() != null) rowSmall(totals, "(" + q.taxRateName() + ")");
            row(totals, "CGST" + suffixPercent(q), money(q.cgstAmount(), q.currency()), false);
            row(totals, "SGST" + suffixPercent(q), money(q.sgstAmount(), q.currency()), false);
        } else if (gtZero(q.igstAmount())) {
            if (q.taxRateName() != null) rowSmall(totals, "(" + q.taxRateName() + ")");
            row(totals, "IGST" + suffixPercent(q), money(q.igstAmount(), q.currency()), false);
        } else if (gtZero(q.taxAmount())) {
            if (q.taxRateName() != null) rowSmall(totals, "(" + q.taxRateName() + ")");
            row(totals, "Tax" + suffixPercent(q), money(q.taxAmount(), q.currency()), false);
        } else {
            row(totals, "Tax (" + (q.taxTreatment() != null ? q.taxTreatment().replace('_', ' ') : "EXEMPT") + ")",
                    money(BigDecimal.ZERO, q.currency()), false);
        }
        row(totals, "GRAND TOTAL", money(q.grandTotal(), q.currency()), true);
        return totals;
    }

    private void addTextSections(Document document, QuotationResponse q) throws Exception {
        section(document, "Payment Terms", q.paymentTerms());
        section(document, "Delivery Terms", q.deliveryTerms());
        section(document, "Notes", q.notes());
        section(document, "Terms & Conditions", q.termsConditions());
    }

    // ---------- low-level helpers ----------

    private void section(Document document, String title, String body) throws Exception {
        if (!notBlank(body)) {
            return;
        }
        document.add(spacer(8));
        document.add(new Paragraph(title.toUpperCase(), bold(9, Color.GRAY)));
        document.add(new Paragraph(body, small(Font.NORMAL, Color.DARK_GRAY)));
    }

    private void row(PdfPTable table, String label, String value, boolean emphasis) {
        Font font = emphasis ? bold(10, NAVY) : small(Font.NORMAL, Color.DARK_GRAY);
        int borders = emphasis ? com.lowagie.text.Rectangle.TOP | com.lowagie.text.Rectangle.BOTTOM
                : com.lowagie.text.Rectangle.NO_BORDER;
        PdfPCell labelCell = new PdfPCell(new Phrase(label, emphasis ? bold(10, NAVY) : small(Font.NORMAL, Color.DARK_GRAY)));
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

    private void rowSmall(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, tiny(Font.ITALIC, Color.GRAY)));
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, bold(9, Color.WHITE)));
        cell.setBackgroundColor(NAVY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(5);
        cell.setPaddingBottom(5);
        return cell;
    }

    private PdfPCell bodyCell(String text, Color bg, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, small(Font.NORMAL, Color.DARK_GRAY)));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(alignment);
        cell.setPaddingTop(4);
        cell.setPaddingBottom(4);
        return cell;
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
        Font font = new Font(Font.HELVETICA, size, Font.BOLD, color);
        return font;
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

    private String suffixPercent(QuotationResponse q) {
        return q.taxRatePercent() != null ? " @ " + strip(q.taxRatePercent()) + "%" : "";
    }

    private String strip(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private boolean gtZero(BigDecimal value) {
        return value != null && value.signum() > 0;
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
