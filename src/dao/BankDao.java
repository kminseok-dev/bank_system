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
import dto.TransferRequestDto;
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

		// 4. 계좌 이체 (출금 -> 입금을 하나의 트랜잭션으로 처리)
		// 반환값: 1 성공, -1 출금계좌 없음, -2 입금계좌 없음, -3 잔액 부족, 0 그 외 오류
		public int transfer(TransferRequestDto requestDto) {
			String selectSql = "select balance from account where account_number = ? for update";
			String updateSql = "update account set balance = balance + ? where account_number = ?";

			conn = DBUtil.dbConnect();
			PreparedStatement updatePst = null;
			try {
				conn.setAutoCommit(false); // 트랜잭션 시작

				// 1) 출금 계좌 조회 및 잔액 확인 (행 잠금)
				pst = conn.prepareStatement(selectSql);
				pst.setString(1, requestDto.getFromAccountNumber());
				rs = pst.executeQuery();
				if (!rs.next()) {
					conn.rollback();
					return -1; // 출금 계좌 없음
				}
				long fromBalance = rs.getLong("balance");
				rs.close();
				pst.close();

				if (fromBalance < requestDto.getAmount()) {
					conn.rollback();
					return -3; // 잔액 부족
				}

				// 2) 입금 계좌 존재 여부 확인 (행 잠금)
				pst = conn.prepareStatement(selectSql);
				pst.setString(1, requestDto.getToAccountNumber());
				rs = pst.executeQuery();
				if (!rs.next()) {
					conn.rollback();
					return -2; // 입금 계좌 없음
				}
				rs.close();
				pst.close();

				// 3) 출금 계좌 차감
				updatePst = conn.prepareStatement(updateSql);
				updatePst.setLong(1, -requestDto.getAmount());
				updatePst.setString(2, requestDto.getFromAccountNumber());
				updatePst.executeUpdate();

				// 4) 입금 계좌 증가
				updatePst.setLong(1, requestDto.getAmount());
				updatePst.setString(2, requestDto.getToAccountNumber());
				updatePst.executeUpdate();

				conn.commit(); // 둘 다 성공해야 확정
				return 1;
			} catch (SQLException e) {
				try {
					if (conn != null) conn.rollback();
				} catch (SQLException rollbackEx) {
					rollbackEx.printStackTrace();
				}
				e.printStackTrace();
				return 0;
			} finally {
				try {
					if (updatePst != null) updatePst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				DBUtil.dbDisconnect(conn, pst, rs);
			}
		}

		// 5. 입금
		public int deposit(String accountNumber, Long amount) {
			int result = 0;
			String sql = "update account set balance = balance + ? where account_number = ?";

			conn = DBUtil.dbConnect();
			try {
				pst = conn.prepareStatement(sql);
				pst.setLong(1, amount);
				pst.setString(2, accountNumber);
				result = pst.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBUtil.dbDisconnect(conn, pst, null);
			}
			return result;
		}

		// 6. 출금
		public int withdraw(String accountNumber, Long amount) {
			int result = 0;
			String sql = "update account set balance = balance - ? where account_number = ?";

			conn = DBUtil.dbConnect();
			try {
				pst = conn.prepareStatement(sql);
				pst.setLong(1, amount);
				pst.setString(2, accountNumber);
				result = pst.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBUtil.dbDisconnect(conn, pst, null);
			}
			return result;
		}

		// 7. 계좌 해지 (계좌를 실제로 삭제). 잔액이 0인 계좌만 삭제
		// 성공 시 1, 대상 없으면(잔액 남았거나 미존재) 0 반환
		public int closeAccount(String accountNumber) {
			int result = 0;
			String sql = "delete from account where account_number = ? and balance = 0";

			conn = DBUtil.dbConnect();
			try {
				pst = conn.prepareStatement(sql);
				pst.setString(1, accountNumber);
				result = pst.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBUtil.dbDisconnect(conn, pst, null);
			}
			return result;
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
