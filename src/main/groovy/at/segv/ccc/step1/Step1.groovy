package at.segv.ccc.step1

import groovy.transform.Canonical
import groovy.transform.builder.Builder;

@Category(String.class)
class Game1 {

    def parseAccount() {
        def split = this.split(" ")

        return Account.builder().user(split[0])
                .balance(Long.parseLong(split[1]))
                .build()
    }

    def parseTrans() {
        def split = this.split(" ")

        return Transaction.builder().from(split[0])
                .to(split[1]).amount(Long.parseLong(split[2]))
                .submitTime(Long.parseLong(split[3])).build()
    }
}

@Canonical
@Builder
class Account {
    String user
    Long balance
}

@Canonical
@Builder
class Transaction {
    String from, to
    Long amount
    Long submitTime
}

use(Game1) {
    lines = getClass().getResource('/in/level1/level1-4.txt').readLines()

    Integer noAccounts = Integer.parseInt(lines[0])
    List<Account> accounts = lines[1..noAccounts].collect { it.parseAccount() }
    def noTrans = Long.parseLong(lines[1 + noAccounts])
    List<Transaction> trans = lines[noAccounts + 2..-1].collect { it.parseTrans() }

    trans.each { t ->
        def fromA = accounts.find({ a -> a.user.equals(t.from) })
        def toA = accounts.find({ a -> a.user.equals(t.to) })
        fromA.balance -= t.amount
        toA.balance += t.amount
    }

    println accounts.size()
    accounts.each { println "$it.user $it.balance" }


}
