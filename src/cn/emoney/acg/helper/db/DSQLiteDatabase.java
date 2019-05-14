package cn.emoney.acg.helper.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import cn.emoney.acg.data.Goods;
import cn.emoney.sky.libs.db.SQLiteAssetHelper;
import cn.emoney.sky.libs.log.Logger;

public class DSQLiteDatabase extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "estockgoods";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_STOCK = "stocklist";
    private static final String TABLE_BK = "bkgroup";

    public static final String KEY_STOCKCODE = "code";
    private static final String KEY_ID = "0 _id";
    public static final String KEY_STOCKNAME = "name";
    private static final String KEY_UPDATEDATE = "time";
    private static final String KEY_PINYIN = "pinyin";

    private static final String KEY_BKCODE = "bk";

    private static String filter = "and ( " + KEY_STOCKCODE + " between '0600000' and '0700000' or " + KEY_STOCKCODE + " between '0300000' and '0400000' or " + KEY_STOCKCODE + " between '1000001' and '1010000' or " + KEY_STOCKCODE + " between '1300000' and '1309999' )";

    public DSQLiteDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DSQLiteDatabase(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public/* synchronized */ArrayList<Goods> querySHSZStockByCode(String codeFilter, int maxNum) {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String[] sqlSelect = {KEY_ID, KEY_STOCKCODE, KEY_STOCKNAME, KEY_PINYIN};
        qb.setTables(TABLE_STOCK);
        Cursor c = qb.query(db, sqlSelect, KEY_STOCKCODE + " like '%" + codeFilter + "%' " + filter, null, null, null, KEY_STOCKCODE + " asc limit 0," + maxNum);
        ArrayList<Goods> cn = new ArrayList<Goods>();
        String strCode = null;
        String name = null;
        Goods goodsName = null;
        int code = 0;
        try {

            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                do {
                    strCode = c.getString(c.getColumnIndex(KEY_STOCKCODE));
                    code = Integer.parseInt(strCode);
                    name = c.getString(c.getColumnIndex(KEY_STOCKNAME));
                    goodsName = new Goods(code, name);
                    cn.add(goodsName);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
        } finally {
            if (c != null)
                c.close();
        }
        return cn;
    }

    public/* synchronized */ArrayList<Goods> querySHSZStockByPinyin(String codeFilter, int maxNum) {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String[] sqlSelect = {KEY_ID, KEY_STOCKCODE, KEY_STOCKNAME, KEY_PINYIN};
        qb.setTables(TABLE_STOCK);
        Cursor c = qb.query(db, sqlSelect, /*
                                            * "name like '%" + searcherFilter + "%' or " +
                                            */KEY_PINYIN + " like '%" + codeFilter + "%' " + filter, null, null, null, KEY_STOCKCODE + " asc limit 0," + maxNum);
        ArrayList<Goods> cn = new ArrayList<Goods>();
        try {

            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                int count = 0;
                do {
                    int code = Integer.parseInt(c.getString(c.getColumnIndex(KEY_STOCKCODE)));
                    String name = c.getString(c.getColumnIndex(KEY_STOCKNAME));
                    Goods goodsName = new Goods(code, name);
                    cn.add(goodsName);
                    count++;
                } while (c.moveToNext());
            }
        } catch (Exception e) {
        } finally {
            if (c != null)
                c.close();
        }
        return cn;
    }

    // select * from stocklist where code like '%codeFilter%' or name like
    // '%nameFilter%';
    public/* synchronized */Cursor queryStocksByCode(String codeFilter) {

        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {KEY_ID, KEY_STOCKCODE, KEY_STOCKNAME, KEY_PINYIN};

        qb.setTables(TABLE_STOCK);
        Cursor c = null;
        try {
            c = qb.query(db, sqlSelect, /*
                                         * "name like '%" + searcherFilter + "%' or " +
                                         */"code like '%" + codeFilter + "%'", null, null, null, "code asc limit 0," + 100);
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;

    }

    public/* synchronized */Cursor queryLenovoByCode(String codeFilter) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery("select " + KEY_STOCKCODE + "," + KEY_STOCKNAME + " from " + TABLE_STOCK + " where " + KEY_STOCKCODE + " in (select " + KEY_BKCODE + " from " + TABLE_BK + " where " + KEY_STOCKCODE + " = '" + codeFilter + "' and " + KEY_BKCODE + " between '2000000' and '3000000') group by " + KEY_STOCKCODE, null);
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    public/* synchronized */ArrayList<Goods> queryStockArrayByCodes(List<Integer> codes) {
        Cursor c = queryStockCursorByCodes(codes);
        ArrayList<Goods> cn = new ArrayList<Goods>();
        String strCode = null;
        String name = null;
        Goods goodsName = null;
        int code = 0;
        try {

            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                do {
                    strCode = c.getString(c.getColumnIndex(KEY_STOCKCODE));
                    code = Integer.parseInt(strCode);
                    name = c.getString(c.getColumnIndex(KEY_STOCKNAME));
                    goodsName = new Goods(code, name);
                    cn.add(goodsName);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
        } finally {
            if (c != null)
                c.close();
        }
        return cn;
    }

    public/* synchronized */Cursor queryStockCursorByCodes(List<Integer> codes) {
        if (codes == null || codes.size() == 0)
            return null;

        StringBuffer buffer = new StringBuffer();
        StringBuffer oderBy = new StringBuffer();
        int len = codes.size();
        oderBy.append(" case ");
        oderBy.append(KEY_STOCKCODE);
        for (int i = 0; i < len; i++) {
            if (codes.get(i) == 0)
                continue;

            buffer.append("'");
            buffer.append(Int2String(codes.get(i) % 10000000, 7));
            buffer.append("'");
            if (i != len - 1)
                buffer.append(",");
            oderBy.append(" when ");
            oderBy.append("'");
            oderBy.append(Int2String(codes.get(i) % 10000000, 7));
            oderBy.append("'");
            oderBy.append(" then ");
            oderBy.append(i);
        }
        oderBy.append(" end");

        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String[] sqlSelect = {KEY_ID, KEY_STOCKCODE, KEY_STOCKNAME, KEY_PINYIN};
        qb.setTables(TABLE_STOCK);
        String results = buffer.toString();
        if (TextUtils.isEmpty(results))
            return null;

        String oderByResults = oderBy.toString();
        Cursor c = qb.query(db, sqlSelect, KEY_STOCKCODE + " in (" + results + " ) ", null, null, null, oderByResults);

        return c;
    }

    public/* synchronized */ArrayList<Goods> queryStockInfosByCode2(String codeFilter, int maxNum) {
        ArrayList<Goods> cn = null;

        try {
            if (Integer.valueOf(codeFilter) <= 0) {
                return cn;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String[] sqlSelect = {KEY_ID, KEY_STOCKCODE, KEY_STOCKNAME, KEY_PINYIN};
        qb.setTables(TABLE_STOCK);
        Cursor c = qb.query(db, sqlSelect, /*
                                            * "name like '%" + searcherFilter + "%' or " +
                                            */KEY_STOCKCODE + " like '%" + codeFilter + "%'", null, null, null, KEY_STOCKCODE + " asc limit 0," + maxNum);

        try {

            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                int count = 0;
                cn = new ArrayList<Goods>();
                do {
                    int code = Integer.parseInt(c.getString(c.getColumnIndex(KEY_STOCKCODE)));
                    String name = c.getString(c.getColumnIndex(KEY_STOCKNAME));
                    Goods goodsName = new Goods(code, name);
                    cn.add(goodsName);
                    count++;
                } while (c.moveToNext());
            }
        } catch (Exception e) {
        } finally {
            if (c != null)
                c.close();
        }
        return cn;
    }



    public/* synchronized */ArrayList<Goods> queryAGStockInfosByCode(String codeFilter, int maxNum) {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String[] sqlSelect = {KEY_ID, KEY_STOCKCODE, KEY_STOCKNAME, KEY_PINYIN};
        qb.setTables(TABLE_STOCK);
        Cursor c = qb.query(db, sqlSelect, "(substr(" + KEY_STOCKCODE + ",1,2) == '06' or " + "substr(" + KEY_STOCKCODE + ",1,3) in('100','130')) and substr(" + KEY_STOCKCODE + ",2,6) like '%" + codeFilter + "%'", null, null, null, "substr(" + KEY_STOCKCODE + ",2,6)" + " asc limit 0," + maxNum);
        ArrayList<Goods> cn = new ArrayList<Goods>();
        try {

            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                int count = 0;
                do {
                    int code = Integer.parseInt(c.getString(c.getColumnIndex(KEY_STOCKCODE)));
                    String name = c.getString(c.getColumnIndex(KEY_STOCKNAME));
                    Goods goodsName = new Goods(code, name);
                    cn.add(goodsName);
                    count++;
                } while (c.moveToNext());
            }
        } catch (Exception e) {
        } finally {
            if (c != null)
                c.close();
            if (db != null) {
                db.close();
            }
        }
        return cn;
    }


    public/* synchronized */ArrayList<Goods> queryStockInfosByCode(String codeFilter, int maxNum) {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String[] sqlSelect = {KEY_ID, KEY_STOCKCODE, KEY_STOCKNAME, KEY_PINYIN};
        qb.setTables(TABLE_STOCK);
        Cursor c = qb.query(db, sqlSelect, "substr(" + KEY_STOCKCODE + ",1,1) in('0','1','2','3') and substr(" + KEY_STOCKCODE + ",2,6) like '%" + codeFilter + "%'", null, null, null, "substr(" + KEY_STOCKCODE + ",2,6)" + " asc limit 0," + maxNum);
        ArrayList<Goods> cn = new ArrayList<Goods>();
        try {

            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                int count = 0;
                do {
                    int code = Integer.parseInt(c.getString(c.getColumnIndex(KEY_STOCKCODE)));
                    String name = c.getString(c.getColumnIndex(KEY_STOCKNAME));
                    Goods goodsName = new Goods(code, name);
                    cn.add(goodsName);
                    count++;
                } while (c.moveToNext());
            }
        } catch (Exception e) {
        } finally {
            if (c != null)
                c.close();
            if (db != null) {
                db.close();
            }
        }
        return cn;
    }


    public/* synchronized */ArrayList<Goods> queryAGStockInfosByPinyin(String codeFilter, int maxNum, boolean bSort) {
        SQLiteDatabase db = getReadableDatabase();
        if (db != null) {

            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            String[] sqlSelect = {KEY_ID, KEY_STOCKCODE, KEY_STOCKNAME, KEY_PINYIN};
            qb.setTables(TABLE_STOCK);

            Cursor c = null;
            if (bSort) {
                c = qb.query(db, sqlSelect, "(substr(" + KEY_STOCKCODE + ",1,2) == '06' or " + "substr(" + KEY_STOCKCODE + ",1,3) in('100','130')) and " + KEY_PINYIN + " like '%" + codeFilter + "%'", null, null, null, "Length(" + KEY_STOCKNAME + ")" + " asc limit 0," + maxNum);
            } else {
                c = qb.query(db, sqlSelect, "(substr(" + KEY_STOCKCODE + ",1,2) == '06' or " + "substr(" + KEY_STOCKCODE + ",1,3) in('100','130')) and " + KEY_PINYIN + " like '" + codeFilter + "%'", null, null, null, KEY_STOCKCODE + " asc limit 0," + maxNum);
            }
            // Cursor c = qb.query(db, sqlSelect, "substr(" + KEY_STOCKCODE +
            // ",1,1) in('0','1','2','3') and " + KEY_PINYIN + " like '" + codeFilter + "%'" /*+
            // " or " + KEY_PINYIN + " like '%" + codeFilter + "%'"*/, null, null, null,
            // KEY_STOCKCODE + " asc limit 0," + 200);
            ArrayList<Goods> cn = new ArrayList<Goods>();
            try {

                if (c != null && c.getCount() > 0) {
                    c.moveToFirst();
                    int count = 0;
                    do {
                        int code = Integer.parseInt(c.getString(c.getColumnIndex(KEY_STOCKCODE)));
                        String name = c.getString(c.getColumnIndex(KEY_STOCKNAME));
                        Goods goodsName = new Goods(code, name);
                        cn.add(goodsName);
                        count++;
                    } while (c.moveToNext());
                }
            } catch (Exception e) {
            } finally {
                if (c != null)
                    c.close();
            }
            return cn;
        }
        return null;
    }

    public/* synchronized */ArrayList<Goods> queryStockInfosByPinyin(String codeFilter, int maxNum, boolean bSort) {
        SQLiteDatabase db = getReadableDatabase();
        if (db != null) {

            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            String[] sqlSelect = {KEY_ID, KEY_STOCKCODE, KEY_STOCKNAME, KEY_PINYIN};
            qb.setTables(TABLE_STOCK);

            Cursor c = null;
            if (bSort) {
                c = qb.query(db, sqlSelect, "substr(" + KEY_STOCKCODE + ",1,1) in('0','1','2','3') and " + KEY_PINYIN + " like '%" + codeFilter + "%'", null, null, null, "Length(" + KEY_STOCKNAME + ")" + " asc limit 0," + maxNum);
            } else {
                c = qb.query(db, sqlSelect, "substr(" + KEY_STOCKCODE + ",1,1) in('0','1','2','3') and " + KEY_PINYIN + " like '" + codeFilter + "%'" /*
                                                                                                                                                       * +
                                                                                                                                                       * " or "
                                                                                                                                                       * +
                                                                                                                                                       * KEY_PINYIN
                                                                                                                                                       * +
                                                                                                                                                       * " like '%"
                                                                                                                                                       * +
                                                                                                                                                       * codeFilter
                                                                                                                                                       * +
                                                                                                                                                       * "%'"
                                                                                                                                                       */, null, null, null, KEY_STOCKCODE + " asc limit 0," + maxNum);
            }
            // Cursor c = qb.query(db, sqlSelect, "substr(" + KEY_STOCKCODE +
            // ",1,1) in('0','1','2','3') and " + KEY_PINYIN + " like '" + codeFilter + "%'" /*+
            // " or " + KEY_PINYIN + " like '%" + codeFilter + "%'"*/, null, null, null,
            // KEY_STOCKCODE + " asc limit 0," + 200);
            ArrayList<Goods> cn = new ArrayList<Goods>();
            try {

                if (c != null && c.getCount() > 0) {
                    c.moveToFirst();
                    int count = 0;
                    do {
                        int code = Integer.parseInt(c.getString(c.getColumnIndex(KEY_STOCKCODE)));
                        String name = c.getString(c.getColumnIndex(KEY_STOCKNAME));
                        Goods goodsName = new Goods(code, name);
                        cn.add(goodsName);
                        count++;
                    } while (c.moveToNext());
                }
            } catch (Exception e) {
            } finally {
                if (c != null)
                    c.close();
            }
            return cn;
        }
        return null;
    }

    public ArrayList<Goods> queryStockInfoByName(String codeFilter, int maxNum, boolean bSort) {
        SQLiteDatabase db = getReadableDatabase();
        if (db != null) {

            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            String[] sqlSelect = {KEY_ID, KEY_STOCKCODE, KEY_STOCKNAME, KEY_PINYIN};
            qb.setTables(TABLE_STOCK);

            Cursor c = null;
            if (bSort) {
                c = qb.query(db, sqlSelect, "substr(" + KEY_STOCKCODE + ",1,1) in('0','1','2','3') and " + KEY_STOCKNAME + " like '%" + codeFilter + "%'", null, null, null, "Length(" + KEY_STOCKNAME + ")" + " asc limit 0," + maxNum);
            } else {
                c = qb.query(db, sqlSelect, "substr(" + KEY_STOCKCODE + ",1,1) in('0','1','2','3') and " + KEY_STOCKNAME + " like '" + codeFilter + "%'" /*
                                                                                                                                                          * +
                                                                                                                                                          * " or "
                                                                                                                                                          * +
                                                                                                                                                          * KEY_PINYIN
                                                                                                                                                          * +
                                                                                                                                                          * " like '%"
                                                                                                                                                          * +
                                                                                                                                                          * codeFilter
                                                                                                                                                          * +
                                                                                                                                                          * "%'"
                                                                                                                                                          */, null, null, null, KEY_STOCKCODE + " asc limit 0," + maxNum);
            }
            ArrayList<Goods> cn = new ArrayList<Goods>();
            try {

                if (c != null && c.getCount() > 0) {
                    c.moveToFirst();
                    int count = 0;
                    do {
                        int code = Integer.parseInt(c.getString(c.getColumnIndex(KEY_STOCKCODE)));
                        String name = c.getString(c.getColumnIndex(KEY_STOCKNAME));
                        Goods goodsName = new Goods(code, name);
                        cn.add(goodsName);
                        count++;
                    } while (c.moveToNext());
                }
            } catch (Exception e) {
            } finally {
                if (c != null)
                    c.close();
            }
            return cn;
        }
        return null;
    }

    public/* synchronized */int queryStockListLatestDate() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select max(" + KEY_UPDATEDATE + ") from " + TABLE_STOCK, null);
        int date = 0;
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            do {
                date = c.getInt(0);
            } while (c.moveToNext());
        }
        c.close();
        return date;
    }

    public/* synchronized */int queryBKListLatestDate() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select max(" + KEY_UPDATEDATE + ") from " + TABLE_BK, null);
        int date = 0;
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            do {
                date = c.getInt(0);
            } while (c.moveToNext());
        }
        c.close();
        return date;
    }

    public/* synchronized */void updateStockInfos(SQLiteDatabase db, Vector<StockInfo> stockInfos) {
        if (stockInfos == null || stockInfos.size() == 0 || db == null) {
            return;
        }
        InsertHelper ih = new InsertHelper(db, TABLE_STOCK);
        for (int i = 0; i < stockInfos.size(); i++) {
            StockInfo stockInfo = stockInfos.get(i);
            if (hasStockInfo(db, stockInfo)) {
                updateStockInfo(db, stockInfo);
            } else {
                final int codeColumn = ih.getColumnIndex("code");
                final int nameColumn = ih.getColumnIndex("name");
                final int pinyinColumn = ih.getColumnIndex("pinyin");
                final int timeColumn = ih.getColumnIndex("time");
                addStockInfo(ih, stockInfo, codeColumn, nameColumn, pinyinColumn, timeColumn);
            }
        }
    }

    public/* synchronized */void updateStockInfo(SQLiteDatabase db, StockInfo stockInfo) {
        if (stockInfo == null) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(KEY_STOCKNAME, stockInfo.mStockName);
        values.put(KEY_PINYIN, stockInfo.mStockPinYin);
        values.put(KEY_UPDATEDATE, String.valueOf(stockInfo.mUpdateDate));
        db.update(TABLE_STOCK, values, KEY_STOCKCODE + "='" + stockInfo.mStockCode + "'", null);
    }

    private/* synchronized */void printStockInfo(String type, StockInfo stockInfo) {}

    public/* synchronized */boolean hasStockInfo(SQLiteDatabase db, StockInfo stockInfo) {
        if (stockInfo == null || db == null) {
            return false;
        }
        Cursor c = db.rawQuery("select * from " + TABLE_STOCK + " where " + KEY_STOCKCODE + "='" + stockInfo.mStockCode + "'", null);
        int record = 0;
        if (c != null) {
            record = c.getCount();
            c.close();
        }
        if (record > 0) {
            return true;
        } else {
            return false;
        }
    }

    // public void updateBKInfos(Vector<BKInfo> bkInfos)
    // {
    // if(bkInfos == null)
    // {
    // return;
    // }
    // SQLiteDatabase db = getWritableDatabase();
    // for(int i = 0; i < bkInfos.size(); i++)
    // {
    // BKInfo bkInfo = bkInfos.get(i);
    // ContentValues values = new ContentValues();
    // values.put(KEY_STOCKCODE, bkInfo.mStockCode);
    // values.put(KEY_BKCODE, bkInfo.mBKCode);
    // values.put(KEY_UPDATEDATE, String.valueOf(bkInfo.mUpdateDate));
    // db.update(TABLE_STOCK, values, KEY_STOCKCODE+"='"+bkInfo.mStockCode+"'",
    // null);
    // }
    // }
    public/* synchronized */void deleteStockInfos(SQLiteDatabase db, Vector<StockInfo> stockInfos) {
        if (stockInfos == null || stockInfos.size() == 0 || db == null) {
            return;
        }
        for (int i = 0; i < stockInfos.size(); i++) {
            StockInfo stockInfo = stockInfos.get(i);
            db.delete(TABLE_STOCK, KEY_STOCKCODE + "='" + stockInfo.mStockCode + "'", null);
        }
    }

    public/* synchronized */void deleteBKInfos(SQLiteDatabase db, Vector<BKInfo> bkInfos) {
        if (bkInfos == null || bkInfos.size() == 0 || db == null) {
            return;
        }
        for (int i = 0; i < bkInfos.size(); i++) {
            BKInfo bkInfo = bkInfos.get(i);
            db.delete(TABLE_BK, KEY_STOCKCODE + "=? and " + KEY_BKCODE + "=?", new String[] {bkInfo.mStockCode, bkInfo.mBKCode});
        }
    }

    public/* synchronized */void addStockInfos(SQLiteDatabase db, Vector<StockInfo> stockInfos) {
        if (stockInfos == null || stockInfos.size() == 0 || db == null) {
            return;
        }
        InsertHelper ih = new InsertHelper(db, TABLE_STOCK);
        final int codeColumn = ih.getColumnIndex("code");
        final int nameColumn = ih.getColumnIndex("name");
        final int pinyinColumn = ih.getColumnIndex("pinyin");
        final int timeColumn = ih.getColumnIndex("time");
        for (int i = 0; i < stockInfos.size(); i++) {
            StockInfo stockInfo = stockInfos.get(i);
            if (hasStockInfo(db, stockInfo)) {
                updateStockInfo(db, stockInfo);
            } else {
                // if(i % 10 == 0)
                // LogoUtils.info("add goods code", stockInfo.mStockCode);
                addStockInfo(ih, stockInfo, codeColumn, nameColumn, pinyinColumn, timeColumn);
            }
        }
    }

    public/* synchronized */void addStockInfo(InsertHelper ih, StockInfo stockInfo, int codeColumn, int nameColumn, int pinyinColumn, int timeColumn) {
        if (stockInfo == null) {
            return;
        }
        ih.prepareForInsert();
        ih.bind(codeColumn, stockInfo.mStockCode);
        ih.bind(nameColumn, stockInfo.mStockName);
        ih.bind(pinyinColumn, stockInfo.mStockPinYin);
        ih.bind(timeColumn, String.valueOf(stockInfo.mUpdateDate));
        ih.execute();
    }

    public/* synchronized */void addStockInfos2(SQLiteDatabase db, Vector<StockInfo> stockInfos) {

        if (stockInfos == null || stockInfos.size() == 0 || db == null) {
            return;
        }


        String sql = "replace into " + TABLE_STOCK + "(code,name,pinyin,time) values(?,?,?,?)";
        SQLiteStatement stat = db.compileStatement(sql);
        for (StockInfo stockInfo : stockInfos) {

            // LogUtil.easylog("insert.code:" + stockInfo.mStockCode);
            stat.bindString(1, stockInfo.mStockCode);
            stat.bindString(2, stockInfo.mStockName);
            stat.bindString(3, stockInfo.mStockPinYin);
            stat.bindString(4, String.valueOf(stockInfo.mUpdateDate));
            stat.executeInsert();
        }

    }

    public/* synchronized */void addBKInfos(SQLiteDatabase db, Vector<BKInfo> bkInfos) {
        if (bkInfos == null || bkInfos.size() == 0) {
            return;
        }
        InsertHelper ih = new InsertHelper(db, TABLE_BK);
        final int codeColumn = ih.getColumnIndex("code");
        final int bkColumn = ih.getColumnIndex("bk");
        final int timeColumn = ih.getColumnIndex("time");
        for (int i = 0; i < bkInfos.size(); i++) {
            BKInfo bkInfo = bkInfos.get(i);
            // if(i % 100 == 0)
            // LogoUtils.info("code", bkInfo.mStockCode);

            // if(!hasBKInfo(db,bkInfo))
            {
                addBKInfo(ih, bkInfo, codeColumn, bkColumn, timeColumn);
            }
        }
    }

    public/* synchronized */void addBKInfo(InsertHelper ih, BKInfo bkInfo, int codeColumn, int bkColumn, int timeColumn) {
        if (bkInfo == null) {
            return;
        }
        ih.prepareForInsert();
        ih.bind(codeColumn, bkInfo.mStockCode);
        ih.bind(bkColumn, bkInfo.mBKCode);
        ih.bind(timeColumn, String.valueOf(bkInfo.mUpdateDate));
        ih.execute();
    }

    public/* synchronized */boolean hasBKInfo(SQLiteDatabase db, BKInfo bkInfo) {
        if (bkInfo == null || db == null) {
            return false;
        }
        Cursor c = db.rawQuery("select * from " + TABLE_BK + " where " + KEY_STOCKCODE + "='" + bkInfo.mStockCode + "' and " + KEY_BKCODE + "='" + bkInfo.mBKCode + "'", null);
        int record = 0;
        if (c != null) {
            record = c.getCount();
            c.close();
        }
        if (record > 0) {
            return true;
        } else {
            return false;
        }
    }

    public/* synchronized */Cursor queryStocksHy(int codeFilter) {

        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"0 _id", "name"};
        String sqlTables = TABLE_STOCK;

        qb.setTables(sqlTables);
        Cursor c = null;
        try {
            c = qb.query(db, sqlSelect, "code = (select bk from bkgroup where code=" + codeFilter + " and bk>2002000 and bk<2003000)", null, null, null, "code asc limit 0," + 10);
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
            }
        } catch (Exception e) {
        }
        return c;

    }

    public/* synchronized */ArrayList<Goods> queryStocksTop20(int topNum) {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String[] sqlSelect = {KEY_ID, KEY_STOCKCODE, KEY_STOCKNAME};
        qb.setTables(TABLE_STOCK);
        Cursor c = null;
        ArrayList<Goods> cn = new ArrayList<Goods>();
        try {
            c = qb.query(db, sqlSelect, null, null, null, null, "code asc limit 0," + topNum);
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                do {
                    int code = Integer.parseInt(c.getString(c.getColumnIndex(KEY_STOCKCODE)));
                    String name = c.getString(c.getColumnIndex(KEY_STOCKNAME));
                    Goods goodsName = new Goods(code, name);
                    cn.add(goodsName);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
        }
        return cn;
    }

    public void outputStockList() {
        // "select * from stocklist where code like '0%' or code like '1%'";
        Logger logger = Logger.create("stocklist");
        SQLiteDatabase db = getReadableDatabase();
        logger.append("[");
        Cursor c = null;
        try {
            c = db.rawQuery("select * from " + TABLE_STOCK + " where code like '0%' or code like '1%'", null);
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                do {
                    String code = c.getString(c.getColumnIndex(KEY_STOCKCODE));
                    String name = c.getString(c.getColumnIndex(KEY_STOCKNAME));
                    logger.append("{\"code\":\"" + code + "\", \"name\":\"" + name + "\"},");
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
        }
        logger.append("]");
        logger.output("stocklist");
    }

    public static String Int2String(int nValue, int nNum) {
        String str = String.valueOf(nValue);
        int n = str.length();
        for (int i = n; i < nNum; i++)
            str = '0' + str;
        return str;
    }

    public static class StockInfo {
        public String mStockName = null;
        public String mStockCode = null;
        public String mStockPinYin = null;
        public int mUpdateDate = 0;
    }

    public static class BKInfo {
        public String mBKCode = null;
        public String mStockCode = null;
        public int mUpdateDate = 0;
    }
}
