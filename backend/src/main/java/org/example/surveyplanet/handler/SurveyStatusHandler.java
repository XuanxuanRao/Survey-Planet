package org.example.surveyplanet.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.example.surveyplanet.pojo.entity.survey.Survey;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class SurveyStatusHandler extends BaseTypeHandler<Survey.Status> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Survey.Status parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getValue());
    }

    @Override
    public Survey.Status getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String status = rs.getString(columnName);
        return status != null ? Survey.Status.fromString(status) : null;
    }

    @Override
    public Survey.Status getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String status = rs.getString(columnIndex);
        return status != null ? Survey.Status.fromString(status) : null;
    }

    @Override
    public Survey.Status getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }
}
