package es.eina.sql.parameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLParameterLong implements ISQLParameter{
	private long param = 0L;
	
	public SQLParameterLong(long param) {
		this.param = param;
	}
	
	public long getParam(){
		return param;
	}
	
	@Override
	public PreparedStatement handleParameter(int index, PreparedStatement p) {
		try {
			p.setLong(index, getParam());
		} catch (SQLException e) {
			System.out.println("Couldn't set param " + index + " with value " + getParam());
			e.printStackTrace();
		}
		return p;
	}
}
