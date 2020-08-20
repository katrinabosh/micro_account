package ru.test.account.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import ru.test.account.Application;
import ru.test.account.exception.SumNotExistException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class AccountServiceTest {

    @Autowired
    private AccountService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    public void createBalanceRequestLessThenAccount() throws SumNotExistException {
        UUID personId =  UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 200000);

        UUID requestId =  UUID.randomUUID();
        service.createBalanceRequest(requestId, personId, BigDecimal.valueOf(199000));

        var result = jdbcTemplate.queryForMap(" select * from balance_request where id = ?", requestId);

        assertTrue(result.size() > 0);
        assertEquals(personId, result.get("person_id"));
        assertEquals(BigDecimal.valueOf(199000), result.get("balance"));
    }

    @Test
    public void createBalanceRequestForOneAccount() throws SumNotExistException {
        UUID personId =  UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 200000);

        UUID requestId =  UUID.randomUUID();
        service.createBalanceRequest(requestId, personId, BigDecimal.valueOf(200000));

        var result = jdbcTemplate.queryForMap(" select * from balance_request where id = ?", requestId);

        assertTrue(result.size() > 0);
        assertEquals(personId, result.get("person_id"));
        assertEquals(BigDecimal.valueOf(200000), result.get("balance"));
    }

    @Test
    public void createBalanceRequestForTwoAccount() throws SumNotExistException {
        UUID personId =  UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 180000);
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380082", 20000);

        UUID requestId =  UUID.randomUUID();
        service.createBalanceRequest(requestId, personId, BigDecimal.valueOf(200000));

        var result = jdbcTemplate.queryForMap(" select * from balance_request where id = ?", requestId);

        assertTrue(result.size() > 0);
        assertEquals(personId, result.get("person_id"));
        assertEquals(BigDecimal.valueOf(200000), result.get("balance"));
    }

    @Test(expected = SumNotExistException.class)
    public void createBalanceRequestForTwoAccountNegative() throws SumNotExistException {
        UUID personId =  UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 180_000);
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380082", 20_000);

        UUID requestId =  UUID.randomUUID();
        service.createBalanceRequest(requestId, personId, BigDecimal.valueOf(200_001));

    }

    @Test(expected = SumNotExistException.class)
    public void createTwoBalanceRequestNegative() throws SumNotExistException {
        UUID personId =  UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380082", 2_000);
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380082", 20_000);

        UUID requestId =  UUID.randomUUID();
        service.createBalanceRequest(requestId, personId, BigDecimal.valueOf(18_000));
        service.createBalanceRequest(requestId, personId, BigDecimal.valueOf(18_000));
    }

    @Test(expected = DuplicateKeyException.class)
    public void createDuplicateBalanceRequest() throws SumNotExistException {
        UUID personId =  UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 200_000);

        UUID requestId =  UUID.randomUUID();
        service.createBalanceRequest(requestId, personId, BigDecimal.valueOf(2_000));

        service.createBalanceRequest(requestId, personId, BigDecimal.valueOf(2_000));
    }

    @Test
    public void createTwoBalanceRequestForOneAccount() throws SumNotExistException {
        UUID personId =  UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 200_000);

        service.createBalanceRequest(UUID.randomUUID(), personId, BigDecimal.valueOf(10_000));
        service.createBalanceRequest(UUID.randomUUID(), personId, BigDecimal.valueOf(150_000));


        var result = jdbcTemplate.queryForMap(
                " select count(*) as result from balance_request where person_id = ?", personId);

        assertTrue(result.size() > 0);
        assertEquals(2L, result.get("result"));
    }

    @Test
    public void executeBalanceRequestForTwoAccount() throws SumNotExistException {
        UUID personId =  UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 180_000);
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380082", 20_000);
        UUID requestId =  UUID.randomUUID();
        service.createBalanceRequest(requestId, personId, BigDecimal.valueOf(200_000));

        service.executeBalanceRequest(requestId, personId, BigDecimal.valueOf(199_400));

        var result = jdbcTemplate.queryForMap(" select count(*) as result from balance_request where person_id = ?", personId);

        assertTrue(result.size() > 0);
        assertEquals(0L, result.get("result"));

        var resultAcc = jdbcTemplate.queryForMap(" select sum(balance) as result from person_account where person_id = ?", personId);

        assertEquals(BigDecimal.valueOf(600L), resultAcc.get("result"));
    }


    @Test
    public void executeTwoBalanceRequestForOneAccount() throws SumNotExistException {
        UUID personId =  UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 200_000);

        UUID requestId =  UUID.randomUUID();
        service.createBalanceRequest(requestId, personId, BigDecimal.valueOf(10_000));
        service.createBalanceRequest(UUID.randomUUID(), personId, BigDecimal.valueOf(150_000));

        service.executeBalanceRequest(requestId, personId, BigDecimal.valueOf(10_000));


        var result = jdbcTemplate.queryForMap(" select count(*) as result from balance_request where person_id = ?", personId);

        assertTrue(result.size() > 0);
        assertEquals(1L, result.get("result"));

        var resultAcc = jdbcTemplate.queryForMap(" select sum(balance) as result from person_account where person_id = ?", personId);

        assertEquals(BigDecimal.valueOf(190_000L), resultAcc.get("result"));
    }

    @Test
    public void cancelBalanceRequest() throws SumNotExistException {
        UUID personId =  UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 200_000);

        UUID requestId =  UUID.randomUUID();
        service.createBalanceRequest(requestId, personId, BigDecimal.valueOf(10_000));
        service.createBalanceRequest(UUID.randomUUID(), personId, BigDecimal.valueOf(150_000));

        service.cancelBalanceRequest(requestId);

        var result = jdbcTemplate.queryForMap(" select count(*) as result from balance_request where person_id = ?", personId);

        assertTrue(result.size() > 0);
        assertEquals(1L, result.get("result"));

        var resultAcc = jdbcTemplate.queryForMap(" select sum(balance) as result from person_account where person_id = ?", personId);

        assertEquals(BigDecimal.valueOf(200_000L), resultAcc.get("result"));
    }

}