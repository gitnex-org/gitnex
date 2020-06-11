package org.mian.gitnex.helpers;

import org.junit.Test;
import org.mian.gitnex.models.FileDiffView;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Author 6543
 */

public class ParseDiffTest {

	private String commitDiff = "diff --git a/blob.bin b/blob.bin\n" + "deleted file mode 100644\n" + "index 0d73bd2..0000000\n" + "Binary files a/blob.bin and /dev/null differ\n" + "diff --git a/newOne.txt b/newOne.txt\n" + "new file mode 100644\n" + "index 0000000..d46eed0\n" + "--- /dev/null\n" + "+++ b/newOne.txt\n" + "@@ -0,0 +1,2 @@\n" + "+a new file\n" + "+is ok\n" + "diff --git a/toDel.txt b/toDel.txt\n" + "deleted file mode 100644\n" + "index db7b61d..0000000\n" + "--- a/toDel.txt\n" + "+++ /dev/null\n" + "@@ -1,6 +0,0 @@\n" + "-fdsafew\n" + "-fcdsafd\n" + "-saf\n" + "-dsa\n" + "-fds\n" + "-af\n" + "diff --git a/wow.txt b/wow.txt\n" + "index 92e7b0a..c6f2550 100644\n" + "--- a/wow.txt\n" + "+++ b/wow.txt\n" + "@@ -1 +1 @@\n" + "-no newLN\n" + "\\ No newline at end of file\n" + "+no newLN";
	private String pullDiff = "diff --git a/blob.bin b/blob.bin\n" + "deleted file mode 100644\n" + "index 0d73bd22356cdedbda15802490dbba5677c0bf82..0000000000000000000000000000000000000000\n" + "GIT binary patch\n" + "literal 0\n" + "HcmV?d00001\n" + "\n" + "literal 1048576\n" + "zcmV(nK=QwPuB28ikRJ&1O;k}2;ZJdPRlcSh&H3ZzL{FUXh$7I70$=OgJ&|C6l9n5^\n" + "z)($UbdKG~u1=kru;-zP`c59V|o(6s6w`nh4BZjq62uO^~OO=?T8h^2Wd<-s#e_iI~\n" + "zURkze=$MhaJ$k?4&39P$(BT6=fYUYoKa5hsMqqt;d=e0W61jUQXj5h&>9Mf9aHlLn\n" + "zL?z4mKo3>xs9>gwR}Cx#ZeNWKy0M;5CRQHq%6NVh5=X<1a8P)FsE7yASIj-Jq{{ex\n" + "z2A~tfI3XyuL5>6ot@$SpZItcakZOhIGXKm-LOOpY_XVQ@3=@2EfJq~TXJfiw$1e+3\n" + "zGr~C0-C$O1g|{ndCbn0qMk{!T3Yz523kz;7D*LkRfhH7eYxM4fpC`qUAu4irPjtJy\n" + "Jx`FK##b8Ms9rXYJ\n" + "diff --git a/newOne.txt b/newOne.txt\n" + "new file mode 100644\n" + "index 0000000..d46eed0\n" + "--- /dev/null\n" + "+++ b/newOne.txt\n" + "@@ -0,0 +1,2 @@\n" + "+a new file\n" + "+is ok\n" + "diff --git a/toDel.txt b/toDel.txt\n" + "deleted file mode 100644\n" + "index db7b61d..0000000\n" + "--- a/toDel.txt\n" + "+++ /dev/null\n" + "@@ -1,6 +0,0 @@\n" + "-fdsafew\n" + "-fcdsafd\n" + "-saf\n" + "-dsa\n" + "-fds\n" + "-af\n" + "diff --git a/wow.txt b/wow.txt\n" + "index 92e7b0a..c6f2550 100644\n" + "--- a/wow.txt\n" + "+++ b/wow.txt\n" + "@@ -1 +1 @@\n" + "-no newLN\n" + "\\ No newline at end of file\n" + "+no newLN";

	@Test
	public void parseCommitDiff() {

		List<FileDiffView> parsed = ParseDiff.getFileDiffViewArray(commitDiff);
		assertEquals(4, parsed.size());
		assertTrue(parsed.get(0).isFileBinary());
		assertFalse(parsed.get(1).isFileBinary());

		assertEquals(1, parsed.get(1).getFileContents().size());
		assertEquals(2, parsed.get(1).getFileContents().get(0).getLineAdded());
		assertEquals(0, parsed.get(1).getFileContents().get(0).getLineRemoved());

		assertEquals(1, parsed.get(3).getFileContents().get(0).getLineAdded());
		assertEquals(1, parsed.get(3).getFileContents().get(0).getLineRemoved());
	}

	@Test
	public void parsePullDiff() {

		List<FileDiffView> parsed = ParseDiff.getFileDiffViewArray(pullDiff);
		assertEquals(4, parsed.size());
		assertTrue(parsed.get(0).isFileBinary());
		assertFalse(parsed.get(1).isFileBinary());

		assertEquals(1, parsed.get(1).getFileContents().size());
		assertEquals(2, parsed.get(1).getFileContents().get(0).getLineAdded());
		assertEquals(0, parsed.get(1).getFileContents().get(0).getLineRemoved());

		assertEquals(1, parsed.get(3).getFileContents().get(0).getLineAdded());
		assertEquals(1, parsed.get(3).getFileContents().get(0).getLineRemoved());
	}

}
