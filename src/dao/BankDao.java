package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import dto.AccountCreateRequestDto;
import dto.AccountCreateResponseDto;
import util.DBUtil;

public class BankDao {
	//DB연결
		Connection conn;
		// SQL문 보내기 위한 통로
		Statement st;
		// SQL문 보내기 위한 통로, ?가능
		PreparedStatement pst;
		// Select 결과를 받음.
		ResultSet rs;
		// DML 결과, 영향을 받은 건수
		int resultCount;
		
		public int insertAccount(AccountCreateRequestDto requestDto, String accountNumber) {
			int result = 0;
			String sql = "insert into account (member_id, customer_name, account_number, account_name, balance, created_at) "
					   + "values (?, ?, ?, ?, ?, sysdate)"; 
			
			conn = DBUtil.dbConnect();
			try {
				pst = conn.prepareStatement(sql);
				pst.setLong(1, requestDto.getId());
				pst.setString(2, requestDto.getCustomerName());
				pst.setString(3, accountNumber);
				pst.setString(4, requestDto.getAccountName());
				pst.setLong(5, requestDto.getInitialBalance());
				
				result = pst.executeUpdate(); // 성공 시 1 반환
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBUtil.dbDisconnect(conn, pst, null);
			}
			return result;
		}

		// 2. 전체 계좌 조회
		public List<AccountCreateResponseDto> selectAll() {
			List<AccountCreateResponseDto> accountList = new ArrayList<>();
			String sql = "select * from account";
			
			conn = DBUtil.dbConnect();
			try {
				st = conn.createStatement();
				rs = st.executeQuery(sql);
				while (rs.next()) {
					AccountCreateResponseDto account = makeAccount(rs);
					accountList.add(account);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBUtil.dbDisconnect(conn, st, rs);
			}
			return accountList;
		}

		// 3. 계좌번호로 단건 계좌 조회
		public AccountCreateResponseDto selectByAccountNumber(String accountNumber) {
			AccountCreateResponseDto account = null;
			String sql = "select * from account where account_number = ?";
			
			conn = DBUtil.dbConnect();
			try {
				pst = conn.prepareStatement(sql);
				pst.setString(1, accountNumber);
				rs = pst.executeQuery();
				
				if (rs.next()) {
					account = makeAccount(rs);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBUtil.dbDisconnect(conn, pst, rs);
			}
			return account;
		}

		// ResultSet을 Response DTO로 매핑하는 헬퍼 메서드
		private AccountCreateResponseDto makeAccount(ResultSet rs) throws SQLException {
			AccountCreateResponseDto account = new AccountCreateResponseDto();
			
			account.setAccountId(rs.getLong("account_id"));
			account.setMemberId(rs.getLong("member_id"));
			account.setCustomerName(rs.getString("customer_name")); 
			account.setAccountNumber(rs.getString("account_number"));
			account.setAccountName(rs.getString("account_name"));
			account.setBalance(rs.getLong("balance")); // 추가
			
			account.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : null);
			
			return account;
		}
}
