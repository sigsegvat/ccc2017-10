package at.segv.ccc.step4

import groovy.transform.Canonical
import groovy.transform.builder.Builder;

@Category(String.class)
class Game1 {

    def parseReq() {
        def s = this.split(" ")
        return TransReq.builder().transId(s[0]).fromOwner(s[1]).toOwner(s[2]).amount(Long.parseLong(s[3])).time(Long.parseLong(s[4])).build()
    }

    def parseTrans() {
        def s = this.split(" ")

        Integer noIn = Integer.parseInt(s[1])
        def ins = s[2..noIn*3+1]
        Integer noOut = Integer.parseInt(s[noIn*3+2])
        def outs = s[noIn*3+3..noIn*3+3 + noOut*2 -1]
        def transtime = Long.parseLong(s[-1])


        def intrans = []
        for(int i = 0; i< ins.size()/3; i++) {
            def inp = ins.subList(i*3,i*3+3)
            intrans.add TransIn.builder().transId(inp[0]).owner(inp[1]).amount(Long.parseLong(inp[2])).build()
        }

        def outtrans = []
        for(int i = 0; i< outs.size()/2; i++) {
            def inp = outs.subList(i*2,i*2+2)
            outtrans.add TransOut.builder().transId(s[0]).used(false).owner(inp[0]).amount(Long.parseLong(inp[1])).build()
        }

        return Transaction.builder()
                .transId(s[0]).noIn(noIn).noOut(noOut).inList(intrans).outList(outtrans)
                .submitTime(transtime).build()
    }
}


@Canonical
@Builder
class Transaction implements Comparable {
    String transId
    Long noIn, noOut
    List<TransIn> inList
    List<TransOut> outList
    Long submitTime

    def isValid() {
        if (inList.collect { it.amount}.find ({it <= 0}) != null) return false
        if (outList.collect { it.amount}.find {it <= 0} != null) return false

        List<Long> inamounts = inList.collect { it.amount }
        List<Long> outamounts = outList.collect { it.amount }
        if (inamounts.sum() != outamounts.sum()) return false

        def unique = outList.collect({ it.owner }).unique()
        if(unique.size() != outList.size()) return false

        return true

    }

    @Override
    int compareTo(Object o) {
        Transaction other = o as Transaction
        return this.submitTime - other.submitTime
    }
}

@Canonical
@Builder
class TransIn {
    String transId, owner
    Long amount

}

@Canonical
@Builder
class TransOut {
    String transId
    String owner
    Long amount
    Boolean used
}

@Canonical
@Builder
class TransReq implements Comparable{
    String transId, fromOwner, toOwner
    Long amount
    Long time

    @Override
    int compareTo(Object o) {
        TransReq other = o as TransReq
        return this.time - other.time
    }
}



use(Game1) {
    Integer level = 4;
    String sublevel = "4";

    String name = "level$level-${sublevel}.txt"
    String fileIn = "/in/level$level/" + name
    String filePath = "out/level$level/"
    String fileOut = filePath + name

    lines = getClass().getResource(fileIn).readLines()

    def noTrans = Integer.parseInt(lines[0])
    List<Transaction> trans = lines[1..noTrans].collect { it.parseTrans() }

    def noTransReq = Integer.parseInt(lines[noTrans +1])
    List<TransReq> req = lines[noTrans+2..noTrans+1+noTransReq].collect({ it.parseReq() }).sort()

    req.each {println it}
    println trans.size()
    println trans.findAll({it.isValid()}).size()

    Map<String,Transaction> valids = [:]

    trans.sort().findAll({it.isValid()}).each { t ->

        List<TransOut> foundOutList = []
        def validIns = t.inList.findAll { inT ->
            if(inT.owner.equals("origin") ) return true
            def prev= valids[inT.transId]
            if(prev == null) return false
            def foundOutT= prev.outList.findAll({ o -> !o.used})
                    .find ({ o-> o.owner.equals(inT.owner) && o.amount == inT.amount})

            if(foundOutT == null || foundOutList.contains(foundOutT)) {
                return false
            }
            foundOutList.add(foundOutT)
        }
        if(validIns.size() ==  t.inList.size()){
            foundOutList.each {o -> o.used = true }
            valids[t.transId] = t
        }
    }


    req.each { treq ->
        def validOuts=  valids.values().collect({ v ->
            def all = v.outList.findAll({ o ->
                return !o.used && o.owner.equals(treq.fromOwner)
            })
            return all
        }).flatten()

        def remaining = treq.amount
        List<TransOut> usedOut = []
        for(TransOut out : validOuts) {
            if(remaining > 0) {
                remaining-= out.amount
                usedOut.add(out)
            }
        }
        if(remaining>0) return
        def List<TransIn> newIns = usedOut.collect({o -> TransIn.builder().transId(o.transId).owner(o.owner).amount(o.amount).build()
        })
        def List<TransOut> newOuts = []
        def treqOut = TransOut.builder().amount(treq.amount).transId(treq.transId).owner(treq.toOwner).used(false).build()
        newOuts.add(treqOut)
        if(remaining<0) {
            def treqOutRem = TransOut.builder().amount(-remaining).transId(treq.transId).owner(treq.fromOwner).used(false).build()
            newOuts.add(treqOutRem)
        }
        def newT= Transaction.builder().transId(treq.transId).noIn(usedOut.size()).inList(newIns).noOut(newOuts.size())
        .outList(newOuts).submitTime(treq.time).build()

        usedOut.each {it.used=true}
        valids[newT.transId] = newT

        println newT
    }

    new File(filePath).mkdirs()
    PrintWriter pw = new PrintWriter(fileOut);
    pw.close();
    def file = new File(fileOut)


    println valids.size()

    file << valids.size() << "\n"


    valids.sort().values().each { t ->
        file << "$t.transId $t.noIn "
        t.inList.each { file << "$it.transId $it.owner $it.amount "}
        file << "$t.noOut "
        t.outList.each { file << "$it.owner $it.amount "}
        file << t.submitTime << "\n"
    }







}
