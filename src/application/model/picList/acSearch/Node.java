package application.model.picList.acSearch;

class Node{
    int count;
    int num;
    Node fail;
    Node[] child;
    public Node(){
        fail=null;
        count=0;
        child=new Node[65536];
    }
}