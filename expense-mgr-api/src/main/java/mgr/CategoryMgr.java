package mgr;

import bean.LargeCategoryBean;
import bean.MediumCategoryBean;
import java.sql.*;
import java.util.Vector;

public class CategoryMgr {

    private DBConnectionMgr pool;

    public CategoryMgr() {
        pool = DBConnectionMgr.getInstance();
    }

    public Vector<LargeCategoryBean> listLargeCategory() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Vector<LargeCategoryBean> vlist = new Vector<>();
        try {
            con = pool.getConnection();
            String sql = "SELECT * FROM large_category ORDER BY large_id ASC";
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                LargeCategoryBean bean = new LargeCategoryBean();
                bean.setLargeId(rs.getInt("large_id"));
                bean.setLargeName(rs.getString("large_name"));
                vlist.add(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return vlist;
    }

    public Vector<MediumCategoryBean> listMediumCategoryByLarge(int largeId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Vector<MediumCategoryBean> vlist = new Vector<>();
        try {
            con = pool.getConnection();
            String sql = "SELECT * FROM medium_category WHERE large_id = ? ORDER BY medium_id ASC";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, largeId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                MediumCategoryBean bean = new MediumCategoryBean();
                bean.setMediumId(rs.getInt("medium_id"));
                bean.setLargeId(rs.getInt("large_id"));
                bean.setMediumName(rs.getString("medium_name"));
                vlist.add(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return vlist;
    }
}
