package service;

import java.util.List;

import dao.BankDao;
import dto.AccountCreateRequestDto;
import dto.AccountCreateResponseDto;
import util.AccountNumberGenerator;

public class Bankservice {
	
	BankDao bankDAO = new BankDao();
	AccountNumberGenerator generator = new AccountNumberGenerator();
	
	// 1. 신규 계좌 개설 서비스
		public int insertService(AccountCreateRequestDto requestDto) {
			// 서비스 계층에서 계좌번호 생성기를 작동시킵니다.
			String newAccountNumber = generator.generateAccNum();
			
			// 생성된 계좌번호를 전달하여 BankDAO의 insert 메서드 호출
			int result = bankDAO.insertAccount(requestDto, newAccountNumber);
			return result;
		}
		
		// 2. 계좌번호로 단건 계좌 조회 서비스
		public AccountCreateResponseDto selectByAccountNumberService(String accountNumber) {
			AccountCreateResponseDto account = bankDAO.selectByAccountNumber(accountNumber);
			return account;
		}
		
		// 3. 전체 계좌 조회 서비스
		public List<AccountCreateResponseDto> selectAllService() {
			return bankDAO.selectAll();
		}
}
