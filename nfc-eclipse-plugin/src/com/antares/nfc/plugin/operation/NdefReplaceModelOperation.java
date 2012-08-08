package com.antares.nfc.plugin.operation;

import java.util.List;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelParent;

public class NdefReplaceModelOperation  implements NdefModelOperation {

	private NdefRecordModelParent root;
	private List<NdefRecordModelNode> previous;
	private List<NdefRecordModelNode> next;
	
	public NdefReplaceModelOperation(NdefRecordModelParent root, List<NdefRecordModelNode> previous, List<NdefRecordModelNode> next) {
		this.root = root;
		this.previous = previous;
		this.next = next;
	}

	@Override
	public void execute() {
		root.setChildren(next);
	}

	@Override
	public void revoke() {
		root.setChildren(previous);
	}

}
