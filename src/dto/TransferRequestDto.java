package dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter@Setter
@NoArgsConstructor
public class TransferRequestDto {
	private String fromAccountNumber; // 출금 계좌번호
	private String toAccountNumber;   // 입금(수취) 계좌번호
	private Long amount;              // 이체 금액
}
