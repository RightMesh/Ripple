package io.left.ripple;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import io.left.rightmesh.android.AndroidMeshManager;
import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.mesh.MeshStateListener;
import io.left.rightmesh.util.RightMeshException;

@RunWith(MockitoJUnitRunner.class)
public class RightMeshConnectorTest {

    // Executes each task synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    private static final int MESH_PORT = 5001;

    @Mock
    private AndroidMeshManager androidMeshManager;
    @Mock
    private RightMeshConnector.OnDataReceiveListener onDataReceiveListener;
    @Mock
    private RightMeshConnector.OnPeerChangedListener onPeerChangedListener;
    @Mock
    private RightMeshConnector.OnConnectSuccessListener onConnectSuccessListener;
    @Mock
    private MeshId meshId;

    private RightMeshConnector SUT;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        SUT = new RightMeshConnector(MESH_PORT);
        SUT.setAndroidMeshManager(androidMeshManager);
        SUT.setOnConnectSuccessListener(onConnectSuccessListener);
        SUT.setOnDataReceiveListener(onDataReceiveListener);
        SUT.setOnPeerChangedListener(onPeerChangedListener);
    }

    @Test
    public void meshStateChanged_successEvent() throws RightMeshException, ClassNotFoundException {
        RightMeshConnector spyRightMeshConnector = Mockito.spy(SUT);

        //Trigger
        spyRightMeshConnector.meshStateChanged(meshId, MeshStateListener.SUCCESS);

        //Verify
        verify(androidMeshManager).bind(MESH_PORT);
        verify(spyRightMeshConnector).meshStateChanged(meshId, MeshStateListener.SUCCESS);
    }

    @Test
    public void stop_isCalled() throws RightMeshException.RightMeshServiceDisconnectedException {
        RightMeshConnector spyRightMeshConnector = Mockito.spy(SUT);

        spyRightMeshConnector.stop();

        verify(androidMeshManager).stop();
        verify(spyRightMeshConnector).stop();
    }

    @Test
    public void toRightMeshWalletActivty_isCalled() throws RightMeshException {
        RightMeshConnector spyRightMeshConnector = Mockito.spy(SUT);

        spyRightMeshConnector.toRightMeshWalletActivty();

        verify(androidMeshManager).showSettingsActivity();
        verify(spyRightMeshConnector).toRightMeshWalletActivty();
    }

    @Test
    public void sentDataReliable_isCalled() throws RightMeshException {
        RightMeshConnector spyRightMeshConnector = Mockito.spy(SUT);
        String payload = "abc";

        spyRightMeshConnector.sentDataReliable(meshId, payload);

        verify(spyRightMeshConnector).sentDataReliable(any(), eq(payload));
    }

}
