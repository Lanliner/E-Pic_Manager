package application.model.picList.acSearch;
import application.model.picList.ImageNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class ACAutomation {
    private static Node root=new Node();
    private static Queue<Node> queue=new LinkedList<Node>();
    private static int finalCounts =0;

    public static int getFinalCounts() {
        return finalCounts;
    }

    private static int fileSize;
    private static String[] word;
    private static String sourname;
    public static int[] fileCheck(ArrayList<ImageNode> imageNodes, String name){
        fileSize=imageNodes.size();
        word=new String[fileSize];;
        sourname = name;
        for(int i=0;i<fileSize;i++){
            word[i]=imageNodes.get(i).getFile().getName();
        }

        for(int i=0;i<fileSize;i++){
            insert(word[i],i);
        }
        build_ac_automation(root);
        int[] array;
        array=query(name);
        return array;
    }


    public static int[] query(String s){
        int count=0;
        Node p=root;
        int[]	counts;
        counts=new int[65536];
        char[] str=s.toCharArray();
        for (int i = 0; i < str.length; i++) {
            int index=str[i];
            while(p.child[index]==null&&p!=root){
                p=p.fail;
            }
            p=p.child[index];
            p=(p==null)?root:p;
            Node temp=p;
            while(temp!=root&&temp.count!=-1){
                counts[count]=temp.num;
                count+=temp.count;
                temp.count=-1;
                temp=temp.fail;
            }
        }

        if(s.lastIndexOf(".") >= 0) {
            String[] sstr = {s.substring(0, s.lastIndexOf(".")), s.substring(s.lastIndexOf("."))};
            System.out.println(s.substring(0, s.lastIndexOf(".")));
            System.out.println(s.substring(s.lastIndexOf(".")));
            for(int i=0;i<fileSize;i++){
                System.out.println(fileSize);
                if(s.lastIndexOf(".") >= 0) {
                    String[] tstr = {
                            word[i].substring(0, word[i].lastIndexOf(".")),
                            word[i].substring(word[i].lastIndexOf("."))
                    };
                    if (sunday(tstr[0], sstr[0]) && sunday(tstr[1], sstr[1])) {
                        counts[count] = i;
                        count++;
                    }
                }
            }
        }
        for(int i=0;i<fileSize;i++){
            if(sunday(word[i],sourname)==true){
                counts[count]=i;
                count++;
            }
        }
        finalCounts = count;

        return counts;
    }
    public static void build_ac_automation(Node root){

        root.fail=null;
        queue.add(root);
        while(!queue.isEmpty()){
            Node temp=queue.poll();
            Node p=null;
            for (int i = 0; i < 65536; i++) {
                if(temp.child[i]!=null){
                    if(temp==root){
                        temp.child[i].fail=root;
                    }else{
                        p=temp.fail;
                        while(p!=null){
                            if(p.child[i]!=null){
                                temp.child[i].fail=p.child[i];
                                break;
                            }
                            p=p.fail;
                        }
                        if(p==null){
                            temp.child[i].fail=root;
                        }
                    }
                    queue.add(temp.child[i]);
                }
            }
        }
    }

    public static void insert(String str,int num){
        if(str.isEmpty()||str==""){
            return;
        }
        Node cnode=root;
        for (int i = 0; i < str.length(); i++) {
            int index=str.charAt(i);
            if(index=='.')  break;
            if(cnode.child[index]==null){
                Node pnode=new Node();
                cnode.child[index]=pnode;
            }
            cnode=cnode.child[index];
        }
        cnode.count=1;
        cnode.num=num;
    }

    public static boolean sunday(String source, String target) {
        char[] tempS = source.toCharArray();
        char[] tempT = target.toCharArray();
        int k=0;
        int j=0;
        if(compare(tempS,tempT,k,j)){
            return true;
        }
        else{
            return false;
        }
    }

    public static boolean compare(char[] tempS, char[] tempT, int j, int k) {
        for(int i=j;i<j+tempT.length;i++){
            if(tempT[i-j]==tempS[i]){
                k++;
                continue;
            }
            else{
                break;
            }
        }
        if(k-j==tempT.length){
            return true;
        }
        k=j+tempT.length;
        if(k<(tempS.length-1)){
            int value = check(tempS[k],tempT);
            int step = -value;
            j=k+step;
            return compare(tempS,tempT,j,j);
        }
        else{
            return false;
        }
    }

    public static int check(char c, char[] tempT) {
        for(int i = tempT.length-1;i>=-1;i--){
            if(i==-1||tempT[i]==c){
                return i;
            }
            else{
                continue;
            }
        }
        return 0;
    }

}