package com.hrms.system.controller;

import com.hrms.common.PageResult;
import com.hrms.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String contractNo,
            @RequestParam(required = false) String contractType,
            @RequestParam(required = false) String contractStatus,
            @RequestParam(required = false) String startDateBegin,
            @RequestParam(required = false) String startDateEnd,
            @RequestParam(required = false) String endDateBegin,
            @RequestParam(required = false) String endDateEnd,
            @RequestParam(required = false) String industryType
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE c.deleted = 0 ");

        if (employeeId != null) {
            where.append(" AND c.employee_id = :employeeId ");
            params.addValue("employeeId", employeeId);
        }
        if (StringUtils.hasText(employeeName)) {
            where.append(" AND e.name LIKE :employeeName ");
            params.addValue("employeeName", "%" + employeeName + "%");
        }
        if (StringUtils.hasText(contractNo)) {
            where.append(" AND c.contract_no LIKE :contractNo ");
            params.addValue("contractNo", "%" + contractNo + "%");
        }
        if (StringUtils.hasText(contractType)) {
            where.append(" AND c.contract_type = :contractType ");
            params.addValue("contractType", contractType);
        }
        if (StringUtils.hasText(contractStatus)) {
            where.append(" AND c.contract_status = :contractStatus ");
            params.addValue("contractStatus", contractStatus);
        }
        if (StringUtils.hasText(startDateBegin)) {
            where.append(" AND c.start_date >= :startDateBegin ");
            params.addValue("startDateBegin", startDateBegin);
        }
        if (StringUtils.hasText(startDateEnd)) {
            where.append(" AND c.start_date <= :startDateEnd ");
            params.addValue("startDateEnd", startDateEnd);
        }
        if (StringUtils.hasText(endDateBegin)) {
            where.append(" AND c.end_date >= :endDateBegin ");
            params.addValue("endDateBegin", endDateBegin);
        }
        if (StringUtils.hasText(endDateEnd)) {
            where.append(" AND c.end_date <= :endDateEnd ");
            params.addValue("endDateEnd", endDateEnd);
        }
        if (StringUtils.hasText(industryType)) {
            where.append(" AND c.industry_type = :industryType ");
            params.addValue("industryType", industryType);
        }

        String countSql = """
                SELECT COUNT(*)
                FROM hr_contract c
                LEFT JOIN hr_employee e ON c.employee_id = e.id
                """ + where;
        Long total = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String dataSql = """
                SELECT c.id,
                       c.employee_id AS employeeId,
                       e.employee_no AS employeeNo,
                       e.name AS employeeName,
                       c.contract_no AS contractNo,
                       c.contract_type AS contractType,
                       CASE c.contract_type
                           WHEN 'LABOR_CONTRACT' THEN '劳动合同'
                           WHEN 'CONFIDENTIALITY_AGREEMENT' THEN '保密协议'
                           WHEN 'NON_COMPETE_AGREEMENT' THEN '竞业协议'
                           WHEN 'SERVICE_AGREEMENT' THEN '劳务协议'
                           WHEN 'REEMPLOYMENT_AGREEMENT' THEN '返聘协议'
                           WHEN 'POSITION_APPOINTMENT_AGREEMENT' THEN '岗位聘任协议'
                           ELSE c.contract_type END AS contractTypeDesc,
                       c.contract_subject AS contractSubject,
                       c.start_date AS startDate,
                       c.end_date AS endDate,
                       c.sign_date AS signDate,
                       c.contract_status AS contractStatus,
                       CASE c.contract_status
                           WHEN 'DRAFT' THEN '草稿'
                           WHEN 'ACTIVE' THEN '生效'
                           WHEN 'EXPIRING' THEN '即将到期'
                           WHEN 'EXPIRED' THEN '已到期'
                           WHEN 'TERMINATED' THEN '终止'
                           ELSE c.contract_status END AS contractStatusDesc,
                       c.renew_count AS renewCount,
                       c.industry_type AS industryType,
                       CASE c.industry_type WHEN 'hospital' THEN '医院' ELSE '企业' END AS industryTypeDesc,
                       c.ext_json AS extJson,
                       c.remark,
                       c.created_time AS createdTime,
                       c.updated_time AS updatedTime
                FROM hr_contract c
                LEFT JOIN hr_employee e ON c.employee_id = e.id
                """ + where + " ORDER BY c.created_time DESC, c.id DESC LIMIT :limit OFFSET :offset";

        return Result.success(PageResult.of(
                namedParameterJdbcTemplate.queryForList(dataSql, params),
                total,
                pageNum,
                pageSize
        ));
    }

    @GetMapping("/{id}")
    public Result<Object> getById(@PathVariable Long id) {
        String contractSql = """
                SELECT c.id,
                       c.employee_id AS employeeId,
                       e.employee_no AS employeeNo,
                       e.name AS employeeName,
                       c.contract_no AS contractNo,
                       c.contract_type AS contractType,
                       CASE c.contract_type
                           WHEN 'LABOR_CONTRACT' THEN '劳动合同'
                           WHEN 'CONFIDENTIALITY_AGREEMENT' THEN '保密协议'
                           WHEN 'NON_COMPETE_AGREEMENT' THEN '竞业协议'
                           WHEN 'SERVICE_AGREEMENT' THEN '劳务协议'
                           WHEN 'REEMPLOYMENT_AGREEMENT' THEN '返聘协议'
                           WHEN 'POSITION_APPOINTMENT_AGREEMENT' THEN '岗位聘任协议'
                           ELSE c.contract_type END AS contractTypeDesc,
                       c.contract_subject AS contractSubject,
                       c.start_date AS startDate,
                       c.end_date AS endDate,
                       c.sign_date AS signDate,
                       c.contract_status AS contractStatus,
                       CASE c.contract_status
                           WHEN 'DRAFT' THEN '草稿'
                           WHEN 'ACTIVE' THEN '生效'
                           WHEN 'EXPIRING' THEN '即将到期'
                           WHEN 'EXPIRED' THEN '已到期'
                           WHEN 'TERMINATED' THEN '终止'
                           ELSE c.contract_status END AS contractStatusDesc,
                       c.renew_count AS renewCount,
                       c.industry_type AS industryType,
                       CASE c.industry_type WHEN 'hospital' THEN '医院' ELSE '企业' END AS industryTypeDesc,
                       c.ext_json AS extJson,
                       c.remark,
                       c.created_time AS createdTime,
                       c.updated_time AS updatedTime
                FROM hr_contract c
                LEFT JOIN hr_employee e ON c.employee_id = e.id
                WHERE c.id = :id AND c.deleted = 0
                """;
        List<Map<String, Object>> contracts = namedParameterJdbcTemplate.queryForList(contractSql, new MapSqlParameterSource("id", id));
        if (contracts.isEmpty()) {
            return Result.error("合同不存在");
        }
        Map<String, Object> contract = contracts.get(0);

        String recordSql = """
                SELECT r.id,
                       r.contract_id AS contractId,
                       r.record_type AS recordType,
                       CASE r.record_type
                           WHEN 'CREATE' THEN '创建'
                           WHEN 'UPDATE' THEN '更新'
                           WHEN 'RENEW' THEN '续签'
                           WHEN 'TERMINATE' THEN '终止'
                           WHEN 'STATUS_CHANGE' THEN '状态变更'
                           ELSE r.record_type END AS recordTypeDesc,
                       r.old_value AS oldValue,
                       r.new_value AS newValue,
                       r.change_reason AS changeReason,
                       r.operator_id AS operatorId,
                       COALESCE(u.real_name, '') AS operatorName,
                       r.created_time AS createdTime
                FROM hr_contract_record r
                LEFT JOIN sys_user u ON r.operator_id = u.id
                WHERE r.contract_id = :id
                ORDER BY r.created_time DESC, r.id DESC
                """;
        contract.put("records", namedParameterJdbcTemplate.queryForList(recordSql, new MapSqlParameterSource("id", id)));
        return Result.success(contract);
    }

    @PostMapping
    public Result<Long> create(@RequestBody Map<String, Object> body) {
        Long employeeId = toLong(body.get("employeeId"));
        if (employeeId == null) {
            return Result.error("员工ID不能为空");
        }
        if (!employeeExists(employeeId)) {
            return Result.error("员工不存在");
        }

        String industryType = String.valueOf(valueOrDefault(body.get("industryType"), "company"));
        String contractNo = generateContractNo(industryType);
        String sql = """
                INSERT INTO hr_contract
                    (employee_id, contract_no, contract_type, contract_subject, start_date, end_date, sign_date, contract_status,
                     renew_count, industry_type, ext_json, remark, created_time, updated_time, deleted)
                VALUES
                    (:employeeId, :contractNo, :contractType, :contractSubject, :startDate, :endDate, :signDate, :contractStatus,
                     0, :industryType, :extJson, :remark, NOW(), NOW(), 0)
                """;
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("employeeId", employeeId)
                .addValue("contractNo", contractNo)
                .addValue("contractType", body.get("contractType"))
                .addValue("contractSubject", body.get("contractSubject"))
                .addValue("startDate", body.get("startDate"))
                .addValue("endDate", body.get("endDate"))
                .addValue("signDate", body.get("signDate"))
                .addValue("contractStatus", valueOrDefault(body.get("contractStatus"), "DRAFT"))
                .addValue("industryType", industryType)
                .addValue("extJson", body.get("extJson"))
                .addValue("remark", body.get("remark")));
        Long id = namedParameterJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        if (id != null) {
            insertContractRecord(id, "CREATE", null, null, "创建合同", toLong(body.get("operatorId")));
        }
        return Result.success(id);
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String sql = """
                UPDATE hr_contract
                SET contract_type = :contractType,
                    contract_subject = :contractSubject,
                    start_date = :startDate,
                    end_date = :endDate,
                    sign_date = :signDate,
                    industry_type = :industryType,
                    ext_json = :extJson,
                    remark = :remark,
                    updated_time = NOW()
                WHERE id = :id AND deleted = 0
                """;
        int rows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("contractType", body.get("contractType"))
                .addValue("contractSubject", body.get("contractSubject"))
                .addValue("startDate", body.get("startDate"))
                .addValue("endDate", body.get("endDate"))
                .addValue("signDate", body.get("signDate"))
                .addValue("industryType", valueOrDefault(body.get("industryType"), "company"))
                .addValue("extJson", body.get("extJson"))
                .addValue("remark", body.get("remark")));
        if (rows > 0) {
            insertContractRecord(id, "UPDATE", null, null, "更新合同信息", toLong(body.get("operatorId")));
        }
        return Result.success(rows > 0);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_contract SET deleted = 1, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", id)
        );
        if (rows > 0) {
            insertContractRecord(id, "TERMINATE", null, null, "删除合同", null);
        }
        return Result.success(rows > 0);
    }

    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        String finalStatus = status;
        if (!StringUtils.hasText(finalStatus) && body != null && body.get("status") != null) {
            finalStatus = String.valueOf(body.get("status"));
        }
        if (!StringUtils.hasText(finalStatus)) {
            return Result.error("状态不能为空");
        }

        List<Map<String, Object>> contractRows = namedParameterJdbcTemplate.queryForList(
                "SELECT contract_status AS contractStatus FROM hr_contract WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", id)
        );
        if (contractRows.isEmpty()) {
            return Result.error("合同不存在");
        }
        String oldStatus = String.valueOf(contractRows.get(0).get("contractStatus"));

        int rows = namedParameterJdbcTemplate.update(
                "UPDATE hr_contract SET contract_status = :status, updated_time = NOW() WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource().addValue("id", id).addValue("status", finalStatus)
        );
        if (rows > 0) {
            Long operatorId = body == null ? null : toLong(body.get("operatorId"));
            insertContractRecord(id, "STATUS_CHANGE", oldStatus, finalStatus, "状态变更", operatorId);
        }
        return Result.success(rows > 0);
    }

    @PostMapping("/{id}/renew")
    public Result<Boolean> renew(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String newEndDate = body.get("newEndDate") == null ? null : String.valueOf(body.get("newEndDate"));
        String newSignDate = body.get("newSignDate") == null ? null : String.valueOf(body.get("newSignDate"));
        if (!StringUtils.hasText(newEndDate) || !StringUtils.hasText(newSignDate)) {
            return Result.error("续签日期不能为空");
        }

        List<Map<String, Object>> contractRows = namedParameterJdbcTemplate.queryForList(
                "SELECT end_date AS endDate, renew_count AS renewCount FROM hr_contract WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", id)
        );
        if (contractRows.isEmpty()) {
            return Result.error("合同不存在");
        }
        Map<String, Object> old = contractRows.get(0);
        Integer oldRenewCount = old.get("renewCount") == null ? 0 : Integer.parseInt(String.valueOf(old.get("renewCount")));

        int rows = namedParameterJdbcTemplate.update("""
                        UPDATE hr_contract
                        SET end_date = :newEndDate,
                            sign_date = :newSignDate,
                            renew_count = IFNULL(renew_count,0) + 1,
                            contract_status = 'ACTIVE',
                            updated_time = NOW()
                        WHERE id = :id AND deleted = 0
                        """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("newEndDate", newEndDate)
                        .addValue("newSignDate", newSignDate)
        );
        if (rows > 0) {
            String oldValue = "原结束日期: " + old.get("endDate") + ", 续签次数: " + oldRenewCount;
            String newValue = "新结束日期: " + newEndDate + ", 续签次数: " + (oldRenewCount + 1);
            insertContractRecord(
                    id,
                    "RENEW",
                    oldValue,
                    newValue,
                    body.get("renewReason") == null ? "续签合同" : String.valueOf(body.get("renewReason")),
                    toLong(body.get("operatorId"))
            );
        }
        return Result.success(rows > 0);
    }

    @GetMapping("/expire-warning/page")
    public Result<PageResult<Map<String, Object>>> pageExpireWarning(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "30") Integer warningDays
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("warningDays", warningDays);

        String where = " WHERE c.deleted = 0 AND c.contract_status = 'ACTIVE' AND c.end_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL :warningDays DAY) ";
        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_contract c " + where,
                params,
                Long.class
        );
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        String dataSql = """
                SELECT c.id,
                       c.employee_id AS employeeId,
                       e.employee_no AS employeeNo,
                       e.name AS employeeName,
                       c.contract_no AS contractNo,
                       c.contract_type AS contractType,
                       CASE c.contract_type
                           WHEN 'LABOR_CONTRACT' THEN '劳动合同'
                           WHEN 'CONFIDENTIALITY_AGREEMENT' THEN '保密协议'
                           WHEN 'NON_COMPETE_AGREEMENT' THEN '竞业协议'
                           WHEN 'SERVICE_AGREEMENT' THEN '劳务协议'
                           WHEN 'REEMPLOYMENT_AGREEMENT' THEN '返聘协议'
                           WHEN 'POSITION_APPOINTMENT_AGREEMENT' THEN '岗位聘任协议'
                           ELSE c.contract_type END AS contractTypeDesc,
                       c.contract_subject AS contractSubject,
                       c.start_date AS startDate,
                       c.end_date AS endDate,
                       c.sign_date AS signDate,
                       c.contract_status AS contractStatus,
                       CASE c.contract_status
                           WHEN 'DRAFT' THEN '草稿'
                           WHEN 'ACTIVE' THEN '生效'
                           WHEN 'EXPIRING' THEN '即将到期'
                           WHEN 'EXPIRED' THEN '已到期'
                           WHEN 'TERMINATED' THEN '终止'
                           ELSE c.contract_status END AS contractStatusDesc,
                       c.renew_count AS renewCount,
                       c.industry_type AS industryType,
                       CASE c.industry_type WHEN 'hospital' THEN '医院' ELSE '企业' END AS industryTypeDesc,
                       c.ext_json AS extJson,
                       c.remark,
                       c.created_time AS createdTime,
                       c.updated_time AS updatedTime
                FROM hr_contract c
                LEFT JOIN hr_employee e ON c.employee_id = e.id
                """ + where + " ORDER BY c.end_date ASC, c.id DESC LIMIT :limit OFFSET :offset";

        return Result.success(PageResult.of(
                namedParameterJdbcTemplate.queryForList(dataSql, params),
                total,
                pageNum,
                pageSize
        ));
    }

    private boolean employeeExists(Long employeeId) {
        Long cnt = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hr_employee WHERE id = :id AND deleted = 0",
                new MapSqlParameterSource("id", employeeId),
                Long.class
        );
        return cnt != null && cnt > 0;
    }

    private String generateContractNo(String industryType) {
        String prefix = "hospital".equalsIgnoreCase(industryType) ? "HT" : "CT";
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String noPrefix = prefix + datePart;
        Long seq = namedParameterJdbcTemplate.queryForObject("""
                SELECT COUNT(*) + 1
                FROM hr_contract
                WHERE contract_no LIKE :prefix
                  AND DATE(created_time) = CURDATE()
                """, new MapSqlParameterSource("prefix", noPrefix + "%"), Long.class);
        if (seq == null) {
            seq = 1L;
        }
        return noPrefix + String.format("%03d", seq);
    }

    private void insertContractRecord(Long contractId,
                                      String recordType,
                                      String oldValue,
                                      String newValue,
                                      String changeReason,
                                      Long operatorId) {
        namedParameterJdbcTemplate.update("""
                        INSERT INTO hr_contract_record
                            (contract_id, record_type, old_value, new_value, change_reason, operator_id, created_time)
                        VALUES
                            (:contractId, :recordType, :oldValue, :newValue, :changeReason, :operatorId, NOW())
                        """,
                new MapSqlParameterSource()
                        .addValue("contractId", contractId)
                        .addValue("recordType", recordType)
                        .addValue("oldValue", oldValue)
                        .addValue("newValue", newValue)
                        .addValue("changeReason", changeReason)
                        .addValue("operatorId", valueOrDefault(operatorId, 1L))
        );
    }

    private Object valueOrDefault(Object value, Object defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
