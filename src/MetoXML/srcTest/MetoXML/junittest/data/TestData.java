package MetoXML.junittest.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestData {
    private int id = 0;

    private String nm = "";

    private byte bv = 0;

    private float fv;

    private byte[] bar;

    private char cv;

    private char[] car;

    private int[] iar;

    private int[][][] iarar;

    private Date date;

    private List<Test2Data> listD = new ArrayList<Test2Data>();


    private Test2Data tData;

    private Test2Data[] tDataAr;

    private Test2Data[] t2DataAr;

    private List<String> listS = new ArrayList<String>();

    private boolean bFlag = false;
    
    private double money = 0;

    private Long l3 = Long.valueOf(0);
    
    public boolean isBFlag() {
		return bFlag;
	}

	public void setBFlag(boolean flag) {
		bFlag = flag;
	}

	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = money;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}



	public String getNm() {
		return nm;
	}



	public void setNm(String nm) {
		this.nm = nm;
	}



	public byte getBv() {
		return bv;
	}



	public void setBv(byte bv) {
		this.bv = bv;
	}



	public float getFv() {
		return fv;
	}



	public void setFv(float fv) {
		this.fv = fv;
	}



	public byte[] getBar() {
		return bar;
	}



	public void setBar(byte[] bar) {
		this.bar = bar;
	}



	public char getCv() {
		return cv;
	}



	public void setCv(char cv) {
		this.cv = cv;
	}



	public char[] getCar() {
		return car;
	}



	public void setCar(char[] car) {
		this.car = car;
	}



	public int[] getIar() {
		return iar;
	}



	public void setIar(int[] iar) {
		this.iar = iar;
	}



	public int[][][] getIarar() {
		return iarar;
	}



	public void setIarar(int[][][] iarar) {
		this.iarar = iarar;
	}



	public Date getDate() {
		return date;
	}



	public void setDate(Date date) {
		this.date = date;
	}



	public List<Test2Data> getListD() {
		return listD;
	}



	public void setListD(List<Test2Data> listD) {
		this.listD = listD;
	}



	public Test2Data getTData() {
		return tData;
	}



	public void setTData(Test2Data data) {
		tData = data;
	}



	public List<String> getListS() {
		return listS;
	}



	public void setListS(List<String> listS) {
		this.listS = listS;
	}


	public TestData()
    {
    }

	public Test2Data[] getT2DataAr() {
		return t2DataAr;
	}

	public void setT2DataAr(Test2Data[] dataAr) {
		t2DataAr = dataAr;
	}

	public Long getL3() {
		return l3;
	}

	public void setL3(Long l3) {
		this.l3 = l3;
	}

	public Test2Data[] getTDataAr() {
		return tDataAr;
	}

	public void setTDataAr(Test2Data[] tDataAr) {
		this.tDataAr = tDataAr;
	}

	
	
}
