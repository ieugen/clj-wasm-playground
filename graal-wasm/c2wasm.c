#include <stdio.h>

void hello(){
  printf("Hello from clojure WebAssembly. \n");
}

int add(int x, int y) {
  return x + y;
}

int main() {
  int number = 1;
  int rows = 10;
  for (int i = 1; i <= rows; i++) {
    for (int j = 1; j <= i; j++) {
      printf("%d ", number);
      ++number;
    }
    printf(".\n");
  }
  hello();
  printf("Addition result is %d \n", add(3,5));
  return 0;
}