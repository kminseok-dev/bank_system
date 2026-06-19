package controller;

import java.util.List;
import java.util.Scanner;

import dto.AccountCreateRequestDto;
import dto.AccountCreateResponseDto;
import dto.TransferRequestDto;
import service.Bankservice;
import view.Bankview;

public class Bankcontroller {
	static Scanner sc = new Scanner(System.in);
	static Bankservice bankService = new Bankservice();
	
	public static void main(String[] args) {
		boolean isRunning = true;
		while(isRunning) {
			// BankView 혹은 기존 통일된 View의 메뉴 출력 메서드 호출
			Bankview.menuDisplay();
			
			int menu = 0;
			try {
				menu = Integer.parseInt(sc.nextLine());
			} catch (NumberFormatException e) {
				System.out.println("! 숫자로만 입력해 주세요.");
				continue;
			}
			
			switch(menu) {
				case 1 -> f_accountInsert();             // 계좌 개설
				case 2 -> f_accountSelectByNumber();     // 계좌번호로 단건 조회
				case 3 -> f_accountSelectAll();          // 전체 계좌 조회
				case 4 -> f_accountTransfer();           // 계좌 이체
				case 99 -> {
					System.out.println("\n뱅킹 시스템을 종료합니다. 이용해 주셔서 감사합니다!");
					isRunning = false;
				}
				default -> System.out.println("! 메뉴에 없는 번호입니다. 다시 선택해 주세요.");
			}
		}
	}

	// 1. 신규 계좌 개설 (고객명, 초기 잔액, 계좌이름 입력)
	private static void f_accountInsert() {
		System.out.println("\n[새로운 계좌 개설]");
		System.out.print("회원 고유 ID >> "); 
		Long memberId = Long.parseLong(sc.nextLine());
		System.out.print("고객명 >> "); 
		String customerName = sc.nextLine();
		System.out.print("초기 입금 잔액 >> "); 
		Long initialBalance = Long.parseLong(sc.nextLine());
		System.out.print("계좌 이름(별칭) >> "); 
		String accountName = sc.nextLine();
		
		// 요구사항에 맞춰 수정된 롬복 기반 DTO에 데이터 세팅
		AccountCreateRequestDto requestDto = new AccountCreateRequestDto();
		requestDto.setId(memberId);
		requestDto.setCustomerName(customerName);
		requestDto.setInitialBalance(initialBalance);
		requestDto.setAccountName(accountName);
		
		// 서비스 실행 (내부에서 AccountNumberGenerator 작동)
		int result = bankService.insertService(requestDto);
		Bankview.printMessage("계좌 개설", result);
	}

	// 2. 계좌번호로 단건 계좌 조회
	private static void f_accountSelectByNumber() {
		System.out.print("조회할 계좌번호를 입력하세요 >> ");
		String accountNumber = sc.nextLine();
		
		AccountCreateResponseDto account = bankService.selectByAccountNumberService(accountNumber);
		Bankview.printAccount(account);
	}

	// 3. 전체 계좌 조회
	private static void f_accountSelectAll() {
		List<AccountCreateResponseDto> aList = bankService.selectAllService();
		Bankview.printAccount(aList);
	}

	// 4. 계좌 이체
	private static void f_accountTransfer() {
		System.out.println("\n[계좌 이체]");
		System.out.print("출금 계좌번호 >> ");
		String fromAccountNumber = sc.nextLine();
		System.out.print("입금 계좌번호 >> ");
		String toAccountNumber = sc.nextLine();
		System.out.print("이체 금액 >> ");

		Long amount;
		try {
			amount = Long.parseLong(sc.nextLine());
		} catch (NumberFormatException e) {
			System.out.println("! 이체 금액은 숫자로만 입력해 주세요.");
			return;
		}

		TransferRequestDto requestDto = new TransferRequestDto();
		requestDto.setFromAccountNumber(fromAccountNumber);
		requestDto.setToAccountNumber(toAccountNumber);
		requestDto.setAmount(amount);

		int result = bankService.transferService(requestDto);
		Bankview.printTransferMessage(result);
	}
}
