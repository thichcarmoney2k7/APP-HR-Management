package service;

import com.google.gson.reflect.TypeToken;
import dto.response.KPISummaryResponse;
import util.ApiClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class KPIClientService {

    // 1. Chạy AI đánh giá KPI & tính thưởng cho 1 nhân viên theo tháng -> POST /api/kpis/evaluate
    public KPISummaryResponse evaluateEmployeeKPI(Long employeeId, String monthYear) throws Exception {
        StringBuilder endpoint = new StringBuilder("/kpis/evaluate?employeeId=" + employeeId);
        if (monthYear != null && !monthYear.isBlank()) {
            String encodedMonth = URLEncoder.encode(monthYear.trim(), StandardCharsets.UTF_8);
            endpoint.append("&monthYear=").append(encodedMonth);
        }
        return ApiClient.post(endpoint.toString(), null, KPISummaryResponse.class);
    }

    // 2. Xem chi tiết KPI của 1 nhân viên trong tháng cụ thể -> GET /api/kpis/employee/{id}
    public KPISummaryResponse getKPIByEmployeeAndMonth(Long employeeId, String monthYear) throws Exception {
        StringBuilder endpoint = new StringBuilder("/kpis/employee/" + employeeId);
        if (monthYear != null && !monthYear.isBlank()) {
            String encodedMonth = URLEncoder.encode(monthYear.trim(), StandardCharsets.UTF_8);
            endpoint.append("?monthYear=").append(encodedMonth);
        }
        return ApiClient.get(endpoint.toString(), KPISummaryResponse.class);
    }

    // 3. Xem toàn bộ lịch sử KPI qua các tháng của 1 nhân viên -> GET /api/kpis/employee/{id}/history
    public List<KPISummaryResponse> getKPIHistoryByEmployee(Long employeeId) throws Exception {
        return ApiClient.getList(
                "/kpis/employee/" + employeeId + "/history",
                new TypeToken<List<KPISummaryResponse>>() {}.getType()
        );
    }

    // 4. Lấy báo cáo KPI toàn công ty theo tháng -> GET /api/kpis?monthYear=...
    public List<KPISummaryResponse> getCompanyKPIByMonth(String monthYear) throws Exception {
        String endpoint = "/kpis";
        if (monthYear != null && !monthYear.isBlank()) {
            String encodedMonth = URLEncoder.encode(monthYear.trim(), StandardCharsets.UTF_8);
            endpoint += "?monthYear=" + encodedMonth;
        }
        return ApiClient.getList(endpoint, new TypeToken<List<KPISummaryResponse>>() {}.getType());
    }
}