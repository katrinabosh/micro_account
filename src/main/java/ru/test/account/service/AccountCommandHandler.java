package ru.test.account.service;

import org.springframework.stereotype.Component;
import ru.test.account.exception.SumNotExistException;
import ru.test.account.model.command.ChangeAccount;
import ru.test.account.model.command.CheckAccount;
import ru.test.account.model.event.StocksRequestCanceled;

@Component
public class AccountCommandHandler {

    private final EventSender eventSender;
    private final AccountService accountService;

    public AccountCommandHandler(EventSender eventSender, AccountService accountService) {
        this.eventSender = eventSender;
        this.accountService = accountService;
    }

    public void checkAccount(CheckAccount command) {
        try {
            accountService.createBalanceRequest(command.getRequestId(), command.getPersonId(), command.getSum());
            eventSender.sendRequestConfirmed(command.getRequestId(), command.getSum());
        } catch (SumNotExistException exc) {
            exc.printStackTrace();
            eventSender.sendRequestRejected(command.getRequestId(), command.getSum());
        }
    }

    public void changeAccount(ChangeAccount command) {
        accountService.executeBalanceRequest(command.getRequestId(), command.getPersonId(), command.getSum());
        eventSender.sendAccountChanged(command.getRequestId(), command.getPersonId(), command.getSum());
    }

    public void cancelBalanceRequest(StocksRequestCanceled event) {
        accountService.cancelBalanceRequest(event.getRequestId());
    }


}
