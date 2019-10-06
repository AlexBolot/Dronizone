package main

import (
	"bufio"
	"fmt"
	"os"
	"strconv"
	"strings"
)

func main() {
	fmt.Println("Welcome to the drone mocker")
	fmt.Println("How much drone_count do you want ?")
	reader := bufio.NewReader(os.Stdin)
	text, _ := reader.ReadString('\n')
	text = strings.ReplaceAll(text,"\n","",)
	drone_count,_ := strconv.Atoi(text)
	drones:=start_drones(drone_count);
	fmt.Println(drones);

}

func start_drones(count int) []int {
	pids := make([]int,count);
	for i:=0;i<count;i++{
		pids[0] = go start_drone()
	}
	return pids
}

func start_drone() int{

}