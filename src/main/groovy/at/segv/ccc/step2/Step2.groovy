package at.segv.ccc.step2

import groovy.transform.Canonical
import groovy.transform.builder.Builder;

@Category(String.class)
class Game1 {

    def parseAccount() {
        def split = this.split(" ")

        return Account.builder().owner(split[0])
                .number(split[1])
                .balance(Long.parseLong(split[2]))
                .limit(Long.parseLong(split[3]))
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
    String owner, number
    Long balance, limit

    def isValid() {
        if (!number.startsWith("CAT")) return false
        def digits = number.subSequence(3, 5)
        def accountId = number.subSequence(5, number.length())
        def valid = true

        valid = accountId =~ /[a-zA-Z]/
        valid = valid && digits =~ /[0-9]/

        if (!valid) return false

        Map<Character, Integer> count = [:]

        accountId.chars.each {
            if (count[it.toLowerCase()] == null) {
                count[it.toLowerCase()] = 0
            }
            if (it.isUpperCase()) {
                count[it.toLowerCase()] = count[it.toLowerCase()] + 1
            } else {
                count[it.toLowerCase()] = count[it.toLowerCase()] - 1
            }
        }



        count.each {
            if (it.value != 0) valid = false
        }

        def checksumstr = accountId + "CAT00"
        Long sum = 0
        checksumstr.chars.each { sum += it }
        def checksum = 98 - sum.mod(97)
        valid = valid && checksum == Integer.parseInt(digits)

        return valid
    }
}

@Canonical
@Builder
class Transaction implements Comparable {
    String from, to
    Long amount
    Long submitTime

    @Override
    int compareTo(Object o) {
        Transaction other = o as Transaction
        return this.submitTime - other.submitTime
    }
}

use(Game1) {
    lines = getClass().getResource('/in/level2/level2-4.txt').readLines()

    Integer noAccounts = Integer.parseInt(lines[0])
    List<Account> accounts = lines[1..noAccounts].collect { it.parseAccount() }.findAll({ it.isValid() })
    def noTrans = Long.parseLong(lines[1 + noAccounts])
    List<Transaction> trans = lines[noAccounts + 2..-1].collect { it.parseTrans() }



    trans.sort().each { t ->
        def fromA = accounts.find({ a -> a.number.equals(t.from) })
        def toA = accounts.find({ a -> a.number.equals(t.to) })

        if (fromA == null || toA == null) {
            return
        }

        if (fromA.balance + fromA.limit >= t.amount) {

            fromA.balance -= t.amount
            toA.balance += t.amount
        }

    }


    println accounts.size()
    accounts.each { println "$it.owner $it.balance" }


}
