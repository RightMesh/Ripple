package io.left.ripple;

import android.app.Application;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.mesh.MeshManager;
import io.left.rightmesh.util.RightMeshException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class MainViewModelTest {
    // Executes each task synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock
    private Application application;
    @Mock
    private RightMeshConnector rightMeshConnector;
    @Mock
    private MeshId mockMeshId;
    @Mock
    private MeshManager.DataReceivedEvent rmEvent;

    private MainViewModel SUT;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        SUT = new MainViewModel(application);
        SUT.setRMConnector(rightMeshConnector);
    }

    @Test
    public void init_isCalled() {
        MainViewModel spyViewModel = Mockito.spy(SUT);

        //Trigger
        spyViewModel.init();

        //verify
        verify(rightMeshConnector).setOnMyMeshIdReceivingListener(any());
        verify(rightMeshConnector).setOnPeerChangedListener(any());
        verify(rightMeshConnector).setOnDataReceiveListener(any());
        verify(spyViewModel).init();
    }

    @Test
    public void toRightMeshWalletActivty_isCalled() throws RightMeshException {
        MainViewModel spyViewModel = Mockito.spy(SUT);

        //Trigger
        spyViewModel.toRightMeshWalletActivty();

        //verify
        verify(rightMeshConnector).toRightMeshWalletActivty();
        verify(spyViewModel).toRightMeshWalletActivty();
    }

    @Test
    public void sendColorMsg_nullTargetMeshId() throws RightMeshException {
        MainViewModel spyViewModel = Mockito.spy(SUT);
        MeshId targetId = null;
        Colour msgColor = Colour.RED;
        String payload = String.valueOf(targetId) + ":" + msgColor;

        //Trigger
        spyViewModel.sendColorMsg(targetId, msgColor);

        //verify
        verify(rightMeshConnector, never()).sentDataReliable(targetId, payload);
        verify(spyViewModel).sendColorMsg(targetId, msgColor);
    }

    @Test
    public void sendColorMsg_targetMeshId() throws RightMeshException {
        MainViewModel spyViewModel = Mockito.spy(SUT);
        MeshId targetId = mockMeshId;
        Colour msgColor = Colour.RED;
        String payload = String.valueOf(targetId) + ":" + msgColor;

        //Trigger
        spyViewModel.sendColorMsg(targetId, msgColor);

        //verify
        verify(rightMeshConnector).sentDataReliable(targetId, payload);
        verify(spyViewModel).sendColorMsg(targetId, msgColor);
    }

    @Test
    public void onCleared_isCalled() throws RightMeshException {
        MainViewModel spyViewModel = Mockito.spy(SUT);

        //Trigger
        spyViewModel.onCleared();

        //verify
        verify(rightMeshConnector).stop();
        verify(spyViewModel).onCleared();
    }
}
