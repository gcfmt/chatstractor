/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gcfmt.whatstractor.data;

import java.sql.*;

public class SQLite {

    private Connection conn;
    private Statement stm;

    public SQLite(String arquivo) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        this.conn = DriverManager.getConnection("jdbc:sqlite:" + arquivo);
        this.stm = this.conn.createStatement();
    }

    public ResultSet Query(String queryText) {
        ResultSet rs;
        try {
            rs = this.stm.executeQuery(queryText);
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
