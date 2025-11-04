package io.github.yajuhua.podcast2.mybatis.typehandler;

import com.google.gson.Gson;
import io.github.yajuhua.podcast2.pojo.entity.CookieCloudInfo;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CookieCloudInfoTypeHandler extends BaseTypeHandler<CookieCloudInfo> {
    private final Gson gson = new Gson();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, CookieCloudInfo parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, gson.toJson(parameter));
    }

    @Override
    public CookieCloudInfo getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public CookieCloudInfo getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public CookieCloudInfo getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    private CookieCloudInfo parseJson(String json) throws SQLException {
        if (json == null) {
            return null;
        }
        try {
            return gson.fromJson(json, CookieCloudInfo.class);
        } catch (Exception e) {
            throw new SQLException("Error parsing JSON to CookieCloudInfo", e);
        }
    }
}
