package com.cvicse.inforreport.exporter;

import com.cvicse.inforreport.api.IReportExporter;
import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.model.Cell;
import com.cvicse.inforreport.model.InforReport;
import com.cvicse.inforreport.model.PageSetup;
import com.cvicse.inforreport.model.Table;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @Target: CSV导出程序
 * @Author: shuaiche
 * @version: 1.0
 * Date: 2009-2-20
 * Time: 11:24:54
 */
public class CSVReportExporter implements IReportExporter {

    private StringBuffer print(Table result) throws ReportException {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < result.getRows().size(); i++) {
            List<Cell> row = (List) result.getRows().get(i).getCells();
            for (int j = 0; j < row.size(); j++) {
                String content = row.get(j).getCellContent();
                buf.append(content + ",");
                System.out.print(content + ",");
            }
        }
        System.out.println();
        buf.append("\r\n");
        return buf;
    }

    /**
     * @param report
     * @param result 把报表内容输出到这个输出流中
     * @throws ReportException
     */
    public void exportReport(InforReport report, OutputStream result) throws ReportException {
        //To change body of implemented methods use File | Settings | File Templates.
        StringBuffer buf = new StringBuffer();
        if (report != null) {
            if (report.getTitle() != null) {
                buf.append(print(report.getTitle()));
            }
            if (report.getBody() != null && report.getBody().getTables() != null) {
                for (int i = 0; i < report.getBody().getTables().length; i++) {
                    Table table = report.getBody().getTables()[i];
                    buf.append(print(table));
                    try {
                        result.write(buf.toString().getBytes());
                    } catch (IOException e) {
                        throw new ReportException("CSV Report Error:");
                    }

                }
            }
            if (report.getSummary() != null) {
                buf.append(print(report.getSummary()));
            }
        }
    }

    /**
     * @param pagesetup
     * @param report
     * @param result
     * @throws ReportException
     */
    public void exportReport(PageSetup pagesetup, InforReport report, OutputStream result) throws ReportException {
        exportReport(report, result);
    }

    /**
     * @return CSV 导出的类型
     */
    public String getExportType() {
        return "CSV";
    }
}
