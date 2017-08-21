public class SomeClass {

    private void doUnbraced() {
for( int i = 0; i < 4; i++ ) {
break;
}
}

    private void doCuddled() {
for( int j = 1; j < 5; j++ ) {
break;
}
}

    private void doUncuddled() {
for( int k = 2; k < 6; k++ ) {
break;
}
}

    private void doEmptyBody() {
for( int l = 3; l < 7; ++l ) {
}
}

    private void doSingleLine() {
for( int m = 4; m < 8; ++m ) {
break;
}
}

    private void doNested() {
for( int n = 5; n < 9; n++ ) {
for( int o = 6; o < 10; o++ ) {
break;
}
}
}

}
