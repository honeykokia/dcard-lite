import { defineStore } from "pinia";
import {computed, ref} from "vue";
import type { Board } from "./board.types";

export const useBoardStore = defineStore('board', () => {

    //State
    const boardList = ref<Board[]>([]);
    const currentBoardId = ref<number | null>(null);

    //Getter
    const currentBoard = computed(() => boardList.value.find( b =>
    b.boardId === currentBoardId.value) ?? null);

    //Action
    function setBoardList(data: Board[]) {
        boardList.value = data;
    }

    function clearBoardList(){
        boardList.value = [];
    }

    return{
        boardList,
        currentBoardId,
        currentBoard,
        setBoardList,
        clearBoardList
    }
})