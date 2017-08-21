public class SomeClass {

    private void doLabeledBreak() {
        int arg = +4;
        first  :
        for( int   i  =  0  ;   i  <  10  ;   i  ++ ) {
            second:
            for( int j=0; j<10; j++ ) {
                if( j == 2 ) {
                    break    first   ;
                }
            }
        }
    }

}
