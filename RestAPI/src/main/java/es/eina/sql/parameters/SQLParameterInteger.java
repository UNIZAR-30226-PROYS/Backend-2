package es.eina.sql.parameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLParameterInteger implements ISQLParameter{
	
	private int param = 0;
	
	public SQLParameterInteger(int param) {
		this.param = param;
	}
	
	public int getParam(){
		return param;
	}
	
	@Override
	public PreparedStatement handleParameter(int index, PreparedStatement p) {
		try {
			p.setInt(index, getParam());
		} catch (SQLException e) {
			System.out.println("Couldn't set param " + index + " with value " + getParam());
			e.printStackTrace();
		}
		return p;
	}

}
