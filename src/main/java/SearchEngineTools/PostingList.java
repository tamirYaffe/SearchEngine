package SearchEngineTools;

import java.util.*;

public class PostingList {
    private List<PostingEntry> postingList;
    private int lastDocId;

    public PostingList() {
        postingList=new ArrayList<>();
    }

    /**
     * creates a posting list represented by docID (not gaps).
     * @param postingList
     */
    public PostingList(String postingList){
        this.postingList=new ArrayList<>();
        postingList=postingList.substring(postingList.indexOf(";")+1);
        String[] splitPostingList=postingList.split(" ");
        for (int i = 0; i < splitPostingList.length-1; i+=2){
            int currDocID=Integer.parseInt(splitPostingList[i]);
            lastDocId+=currDocID;
            this.postingList.add(new PostingEntry(lastDocId,Integer.parseInt(splitPostingList[i+1])));
        }
    }

    public static int calculateLastDocID(String postingList) {
        String[] splitPostingList=postingList.split(" ");
        int lastDocId=0;
        for (int i = 0; i < splitPostingList.length-1; i+=2){
            int currDocID= 0;
            try {
                currDocID = Integer.parseInt(splitPostingList[i]);
            } catch (NumberFormatException e) {
                System.out.println(splitPostingList[i]);
            }
            lastDocId+=currDocID;
        }
        return lastDocId;
    }

    private void addAll(List<PostingEntry> other) {
        for(PostingEntry postingEntry:other)
            add(postingEntry);
    }

    public int add(PostingEntry postingEntry) {
        if(postingList.isEmpty()){
            postingList.add(postingEntry);
            lastDocId=postingEntry.getDocID();
            return postingEntry.getSizeInBytes();
        }
        else {
            PostingEntry gapPostingEntry;
            int docId = postingEntry.getDocID();
            int termTF = postingEntry.getTermTF();
            if(docId>lastDocId){
                gapPostingEntry=new PostingEntry(docId-lastDocId,termTF);
                postingList.add(gapPostingEntry);
                lastDocId=docId;
                return gapPostingEntry.getSizeInBytes();
            }
            else{
                int currDocID=0;
                int prevDocID=0;
                PostingEntry currPostingEntry;
                for (int i = 0; i < postingList.size(); i++) {
                    currPostingEntry=postingList.get(i);
                    prevDocID=currDocID;
                    currDocID+=currPostingEntry.getDocID();
                    if(docId<currDocID){
                        gapPostingEntry=new PostingEntry(docId-prevDocID,termTF);
                        currPostingEntry.setDocID(currDocID-docId);
                        postingList.add(i,gapPostingEntry);
                        return gapPostingEntry.getSizeInBytes();
                    }
                }
            }


        }
        return -1;
    }

    /**
     * merge two posting lists represented by docID's to merged list represented by gaps.
     * @param list1
     * @param list2
     * @return
     */
    public static PostingList mergeLists(PostingList list1,PostingList list2){
        PostingList mergedList=new PostingList();
        while (list1.postingList.size() > 0 && list2.postingList.size() > 0) {
            if (list1.postingList.get(0).compareTo(list2.postingList.get(0)) < 0) {
                mergedList.add(list1.postingList.get(0));
                list1.postingList.remove(0);
            }
            else {
                mergedList.add(list2.postingList.get(0));
                list2.postingList.remove(0);
            }
        }

        if (list1.postingList.size() > 0) {
            mergedList.addAll(list1.postingList);
        }
        else if (list2.postingList.size() > 0) {
            mergedList.addAll(list2.postingList);
        }
        return mergedList;
    }

    public int getLastDocId() {
        return lastDocId;
    }

    @Override
    public String toString() {
        String s="";
        boolean first=true;
        for (PostingEntry postingEntry : postingList){
            if(first){
                first=false;
                s+=""+postingEntry;
            }
            else
                s+=" " + postingEntry;
        }
        return s;
    }
}
