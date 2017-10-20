package at.segv.ccc.step1

import groovy.transform.Canonical
import groovy.transform.builder.Builder;

@Category(String.class)
class Game1 {

    def parse1() {
        def split = this.split(" ")
        return Data.builder().data(split[0])
                .x(split[1] as Integer)
                .y(split[2] as Integer)
                .build()
    }


}

@Canonical
@Builder
class Data {

    String data
    Integer x, y
}

lines = getClass().getResource('/in/step1/input1.txt').readLines()


use(Game1) {

    println lines[1].parse1()
}
