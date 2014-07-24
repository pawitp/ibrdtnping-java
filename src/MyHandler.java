import ibrdtn.api.APIException;
import ibrdtn.api.ExtendedClient;
import ibrdtn.api.object.Block;
import ibrdtn.api.object.Bundle;
import ibrdtn.api.object.BundleID;
import ibrdtn.api.sab.CallbackHandler;
import ibrdtn.api.sab.Custody;
import ibrdtn.api.sab.StatusReport;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;


public class MyHandler implements CallbackHandler {

	private ExtendedClient mExClient;
	private ExecutorService mExecutor;
	private Bundle mBundle;
	private ByteArrayOutputStream mBaos;

	public MyHandler(ExtendedClient exClient, ExecutorService executor) {
		mExClient = exClient;
		mExecutor = executor;
		mBaos = new ByteArrayOutputStream();
	}

	@Override
	public void notify(final BundleID id) {
		System.out.println("notify bundle " + id);

		mExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("Loading bundle");
					mExClient.loadBundle(id);
					System.out.println("Getting bundle");
					mExClient.getBundle();
				} catch (APIException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void notify(StatusReport r) {
		System.out.println("notify status " + r);
	}

	@Override
	public void notify(Custody c) {
		System.out.println("notify custody " + c);
	}

	@Override
	public void startBundle(Bundle bundle) {
		System.out.println("startBundle " + bundle);
		mBundle = bundle;
	}

	@Override
	public void endBundle() {
		System.out.println("endBundle");

		// Mark as delivered
		mExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("Marking as delivered");
					mExClient.markDelivered(new BundleID(mBundle));
				} catch (APIException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void startBlock(Block block) {
		System.out.println("startBlock " + block);
	}

	@Override
	public void endBlock() {
		System.out.println("endBlock");
		System.out.println("Received Data: " + new String(mBaos.toByteArray()));
		mBaos.reset();
	}

	@Override
	public OutputStream startPayload() {
		System.out.println("startPayload");
		return mBaos;
	}

	@Override
	public void endPayload() {
		System.out.println("endPayload");
	}

	@Override
	public void progress(long pos, long total) {
		System.out.println("progress " + pos + "/" + total);
	}

}
