package mgr;

import bean.ItemBean;
import bean.PurchaseBean;
import java.sql.*;
import java.util.Vector;

public class StoreMgr {

    private DBConnectionMgr pool;

    public StoreMgr() {
        pool = DBConnectionMgr.getInstance();
    }

    public Vector<ItemBean> listItems() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Vector<ItemBean> vlist = new Vector<>();
        try {
            con = pool.getConnection();
            String sql = "SELECT * FROM item ORDER BY CASE WHEN product_type = '스킨' THEN 1 ELSE 2 END, item_id ASC";
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ItemBean bean = new ItemBean();
                bean.setItemId(rs.getInt("item_id"));
                bean.setProductName(rs.getString("product_name"));
                bean.setProductType(rs.getString("product_type"));
                bean.setPricePoint(rs.getInt("price_point"));
                vlist.add(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return vlist;
    }

    public boolean buyItem(int userId, int itemId) {
        Connection con = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        PreparedStatement pstmt3 = null;
        ResultSet rs = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql1 = "SELECT price_point, product_name FROM item WHERE item_id = ?";
            pstmt1 = con.prepareStatement(sql1);
            pstmt1.setInt(1, itemId);
            rs = pstmt1.executeQuery();
            if (rs.next()) {
                int price = rs.getInt("price_point");
                String pName = rs.getString("product_name");

                PointMgr pointMgr = new PointMgr();
                if (pointMgr.spendPoint(userId, "구매:" + pName, price)) {
                    String sql3 = "INSERT INTO purchase (user_id, item_id, used_point) VALUES (?, ?, ?)";
                    pstmt3 = con.prepareStatement(sql3);
                    pstmt3.setInt(1, userId);
                    pstmt3.setInt(2, itemId);
                    pstmt3.setInt(3, price);
                    if (pstmt3.executeUpdate() == 1) flag = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pstmt3 != null) try { pstmt3.close(); } catch (Exception e) {}
            if (pstmt2 != null) try { pstmt2.close(); } catch (Exception e) {}
            pool.freeConnection(con, pstmt1, rs);
        }
        return flag;
    }
}
