package at.o2xfs.xfs.std;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.o2xfs.common.Library;
import at.o2xfs.memory.core.Address;
import at.o2xfs.memory.core.MemorySystem;
import at.o2xfs.memory.databind.win32.ULongWrapper;
import at.o2xfs.memory.impl.win32.Win32MemorySystem;
import at.o2xfs.xfs.api.OpenRequest;
import at.o2xfs.xfs.api.OpenResponse;
import at.o2xfs.xfs.api.RequestId;
import at.o2xfs.xfs.api.ServiceId;
import at.o2xfs.xfs.api.StartUpException;
import at.o2xfs.xfs.api.VersionsRequired;
import at.o2xfs.xfs.api.WfsVersion;
import at.o2xfs.xfs.api.XfsApi;
import at.o2xfs.xfs.api.XfsConstant;
import at.o2xfs.xfs.api.XfsEventClass;
import at.o2xfs.xfs.api.XfsException;
import at.o2xfs.xfs.api.XfsExceptionFactory;
import at.o2xfs.xfs.api.XfsVersion;
import at.o2xfs.xfs.databind.XfsEnum32Wrapper;
import at.o2xfs.xfs.databind.XfsEnumSet32Wrapper;

public class StdXfsApi implements XfsApi {

	static {
		Library.loadLibrary("o2xfs-xfs-std");
	}

	private static final Logger LOG = LogManager.getLogger(StdXfsApi.class);

	private final MemorySystem memorySystem;

	public StdXfsApi() {
		memorySystem = Win32MemorySystem.INSTANCE;
		loadLibrary0();
	}

	private native void loadLibrary0();

	private native int wfmSetTraceLevel(byte[] hService, byte[] dwTraceLevel);

	private native int wfsAsyncClose(byte[] hService, byte[] hWnd, byte[] lpRequestId);

	private native int wfsAsyncDeregister(byte[] hService, byte[] dwEventClass, byte[] hWndReg, byte[] hWnd,
			byte[] lpRequestId);

	private native int wfsAsyncExecute(byte[] hService, byte[] dwCommand, byte[] lpCmdData, byte[] dwTimeOut,
			byte[] hWnd, byte[] lpRequestId);

	private native int wfsAsyncGetInfo(byte[] hService, byte[] dwCategory, byte[] lpQueryDetails, byte[] dwTimeOut,
			byte[] hWnd, byte[] lpRequestId);

	private native int wfsAsyncLock(byte[] hService, byte[] dwTimeOut, byte[] hWnd, byte[] lpRequestId);

	private native int wfsAsyncOpen(byte[] lpszLogicalName, byte[] hApp, byte[] lpszAppID, byte[] dwTraceLevel,
			byte[] dwTimeOut, byte[] lphService, byte[] hWnd, byte[] dwSrvcVersionsRequired, byte[] lpSrvcVersion,
			byte[] lpSPIVersion, byte[] lpRequestId);

	private native int wfsAsyncRegister(byte[] hService, byte[] dwEventClass, byte[] hWndReg, byte[] hWnd,
			byte[] lpRequestId);

	private native int wfsAsyncUnlock(byte[] hService, byte[] hWnd, byte[] lpRequestId);

	private native int wfsCancelAsyncRequest(byte[] hService, byte[] requestId);

	private native int wfsCleanUp();

	private native int wfsCreateAppHandle(byte[] lphApp);

	private native int wfsDestroyAppHandle(byte[] hApp);

	private native int wfsFreeResult(byte[] lpResult);

	private native boolean wfsIsBlocking();

	private native int wfsStartUp(byte[] dwVersionsRequired, byte[] lpWfsVersion);

	@Override
	public RequestId asyncClose(ServiceId serviceId, Address hWnd) throws XfsException {
		RequestId requestId;
		Address hService = memorySystem.write(serviceId);
		Address lpRequestId = memorySystem.write(memorySystem.nullValue());
		try {
			int errorCode = wfsAsyncClose(hService.getValue(), hWnd.getValue(), lpRequestId.getValue());
			requestId = memorySystem.read(lpRequestId, RequestId.class);
			LOG.info("WFSAsyncClose: errorCode={},requestId={},serviceId={},hWnd={}", errorCode, requestId, serviceId,
					hWnd);
			if (errorCode != 0) {
				throw XfsExceptionFactory.create(errorCode);
			}
		} finally {
			memorySystem.free(hService);
			memorySystem.free(lpRequestId);
		}
		return requestId;
	}

	@Override
	public RequestId asyncDeregister(ServiceId serviceId, Set<XfsEventClass> eventClasses, Address hWndReg,
			Address hWnd) throws XfsException {
		RequestId requestId;
		Address hService = memorySystem.write(serviceId);
		Address dwEventClass = memorySystem.write(XfsEnumSet32Wrapper.build(eventClasses));
		Address lpRequestId = memorySystem.write(memorySystem.nullValue());
		try {
			int errorCode = wfsAsyncDeregister(hService.getValue(), dwEventClass.getValue(), hWndReg.getValue(),
					hWnd.getValue(), lpRequestId.getValue());
			requestId = memorySystem.read(lpRequestId, RequestId.class);
			LOG.info("WFSAsyncDeregister: errorCode={},requestId={},serviceId={},eventClasses={},hWndReg={},hWnd={}",
					errorCode, requestId, serviceId, eventClasses, hWndReg, hWnd);
			if (errorCode != 0) {
				throw XfsExceptionFactory.create(errorCode);
			}
		} finally {
			memorySystem.free(hService);
			memorySystem.free(dwEventClass);
			memorySystem.free(lpRequestId);
		}
		return requestId;
	}

	@Override
	public <E extends Enum<E> & XfsConstant> RequestId asyncExecute(ServiceId serviceId, E command,
			Optional<Object> cmdData, OptionalInt timeOut, Address hWnd) throws XfsException {
		LOG.info("WFSAsyncExecute: serviceId={},command={},cmdData={},timeOut={},hWnd={}", serviceId, command, cmdData,
				timeOut, hWnd);
		RequestId result;
		Address hService = memorySystem.write(serviceId);
		Address dwCommand = memorySystem.write(XfsEnum32Wrapper.build(command));
		Address lpCmdData = cmdData.isEmpty() ? null : memorySystem.write(cmdData.get());
		Address dwTimeOut = memorySystem.write(ULongWrapper.build(timeOut.orElseGet(() -> 0)));
		Address lpRequestId = memorySystem.write(memorySystem.nullValue());
		try {
			int errorCode = wfsAsyncExecute(hService.getValue(), dwCommand.getValue(),
					cmdData.isEmpty() ? null : lpCmdData.getValue(), dwTimeOut.getValue(), hWnd.getValue(),
					lpRequestId.getValue());
			if (errorCode != 0) {
				LOG.info("WFSAsyncExecute: errorCode={}", errorCode);
				throw XfsExceptionFactory.create(errorCode);
			}
			result = memorySystem.read(lpRequestId, RequestId.class);
		} finally {
			memorySystem.free(hService);
			memorySystem.free(dwCommand);
			memorySystem.free(lpCmdData);
			memorySystem.free(dwTimeOut);
			memorySystem.free(lpRequestId);
		}
		return result;
	}

	@Override
	public <E extends Enum<E> & XfsConstant> RequestId asyncGetInfo(ServiceId serviceId, E category,
			Optional<Object> queryDetails, OptionalInt timeOut, Address hWnd) throws XfsException {
		LOG.info("WFSAsyncGetInfo: serviceId={},category={},queryDetails={},timeOut={},hWnd={}", serviceId, category,
				queryDetails, timeOut, hWnd);
		RequestId result;
		Address hService = memorySystem.write(serviceId);
		Address dwCategory = memorySystem.write(XfsEnum32Wrapper.build(category));
		Address lpQueryDetails = queryDetails.isEmpty() ? null : memorySystem.write(queryDetails.get());
		Address dwTimeOut = memorySystem.write(ULongWrapper.build(timeOut.orElseGet(() -> 0)));
		Address lpRequestId = memorySystem.write(memorySystem.nullValue());
		try {
			int errorCode = wfsAsyncGetInfo(hService.getValue(), dwCategory.getValue(),
					queryDetails.isEmpty() ? null : lpQueryDetails.getValue(), dwTimeOut.getValue(), hWnd.getValue(),
					lpRequestId.getValue());
			if (errorCode != 0) {
				LOG.info("WFSAsyncGetInfo: errorCode={}", errorCode);
				throw XfsExceptionFactory.create(errorCode);
			}
			result = memorySystem.read(lpRequestId, RequestId.class);
		} finally {
			memorySystem.free(hService);
			memorySystem.free(dwCategory);
			memorySystem.free(lpQueryDetails);
			memorySystem.free(dwTimeOut);
			memorySystem.free(lpRequestId);
		}
		return result;
	}

	@Override
	public RequestId asyncLock(ServiceId serviceId, OptionalInt timeOut, Address hWnd) throws XfsException {
		LOG.info("WFSAsyncLock: serviceId={},timeOut={},hWnd={}", serviceId, timeOut, hWnd);
		RequestId result;
		Address hService = memorySystem.write(serviceId);
		Address dwTimeOut = memorySystem.write(ULongWrapper.build(timeOut.orElseGet(() -> 0)));
		Address lpRequestId = memorySystem.write(memorySystem.nullValue());
		try {
			int errorCode = wfsAsyncLock(hService.getValue(), dwTimeOut.getValue(), hWnd.getValue(),
					lpRequestId.getValue());
			if (errorCode != 0) {
				LOG.info("WFSAsyncLock: errorCode={}", errorCode);
				throw XfsExceptionFactory.create(errorCode);
			}
			result = memorySystem.read(lpRequestId, RequestId.class);
		} finally {
			memorySystem.free(hService);
			memorySystem.free(dwTimeOut);
			memorySystem.free(lpRequestId);
		}
		return result;
	}

	@Override
	public <E extends Enum<E> & XfsConstant> OpenResponse asyncOpen(OpenRequest request) throws XfsException {
		LOG.info("WFSAsyncOpen: {}", request);
		Address lpszLogicalName = memorySystem.write(request.getLogicalName());
		Address hApp = memorySystem.write(request.getAppHandle().orElse(memorySystem.nullValue()));
		Address lpszAppId = null;
		if (request.getAppId().isPresent()) {
			lpszAppId = memorySystem.write(request.getAppId().get());
		}
		Address dwTraceLevel = memorySystem.write(ULongWrapper.build(request.getTraceLevel()));
		Address dwTimeOut = memorySystem.write(ULongWrapper.build(request.getTimeOut()));
		Address lphService = memorySystem.write(memorySystem.nullValue());
		Address dwSrvcVersionsRequired = memorySystem.write(request.getSrvcVersionsRequired());
		Address lpSrvcVersion = memorySystem.write(WfsVersion.empty());
		Address lpSpiVersion = memorySystem.write(WfsVersion.empty());
		Address lpRequestId = memorySystem.write(RequestId.of(0L));
		LOG.debug(
				"WFSAsyncOpen: lpszLogicalName={},hApp={},lpszAppId={},dwTraceLevel={},dwTimeOut={},lphService={},hWnd={},dwSrvcVersionsRequired={},lpSrvcVersion={},lpSpiVersion={},lpRequestId={}",
				lpszLogicalName, hApp, lpszAppId, dwTraceLevel, dwTimeOut, lphService,
				request.getWindowHandle().getValue(), dwSrvcVersionsRequired, lpSrvcVersion, lpSpiVersion, lpRequestId);
		int errorCode = wfsAsyncOpen(lpszLogicalName.getValue(), hApp.getValue(), lpszAppId.getValue(),
				dwTraceLevel.getValue(), dwTimeOut.getValue(), lphService.getValue(),
				request.getWindowHandle().getValue(), dwSrvcVersionsRequired.getValue(), lpSrvcVersion.getValue(),
				lpSpiVersion.getValue(), lpRequestId.getValue());
		OpenResponse result;
		try {
			WfsVersion spiVersion = memorySystem.read(lpSpiVersion, WfsVersion.class);
			ServiceId serviceId = memorySystem.read(lphService, ServiceId.class);
			WfsVersion srvcVersion = memorySystem.read(lpSrvcVersion, WfsVersion.class);
			RequestId requestId = memorySystem.read(lpRequestId, RequestId.class);
			LOG.info("WFSAsyncOpen: errorCode={},requestId={},serviceId={},srvcVersion={},spiVersion={}", errorCode,
					requestId, serviceId, srvcVersion, spiVersion);
			if (errorCode != 0) {
				throw XfsExceptionFactory.create(errorCode);
			}
			result = OpenResponse.build(requestId, serviceId, srvcVersion, spiVersion);
		} finally {
			memorySystem.free(lpszLogicalName);
			memorySystem.free(hApp);
			if (lpszAppId != null) {
				memorySystem.free(lpszAppId);
			}
			memorySystem.free(dwTraceLevel);
			memorySystem.free(dwTimeOut);
			memorySystem.free(lphService);
			memorySystem.free(dwSrvcVersionsRequired);
			memorySystem.free(lpSrvcVersion);
			memorySystem.free(lpSpiVersion);
			memorySystem.free(lpRequestId);
		}
		return result;
	}

	@Override
	public RequestId asyncRegister(ServiceId serviceId, Set<XfsEventClass> eventClasses, Address hWndReg, Address hWnd)
			throws XfsException {
		RequestId requestId;
		Address hService = memorySystem.write(serviceId);
		Address dwEventClass = memorySystem.write(XfsEnumSet32Wrapper.build(eventClasses));
		Address lpRequestId = memorySystem.write(memorySystem.nullValue());
		try {
			LOG.debug("WFSAsyncRegister: hService={},dwEventClass={},hWndReg={},hWnd={},lpRequestId={}", hService,
					dwEventClass, hWndReg, hWnd, lpRequestId);
			int errorCode = wfsAsyncRegister(hService.getValue(), dwEventClass.getValue(), hWndReg.getValue(),
					hWnd.getValue(), lpRequestId.getValue());
			requestId = memorySystem.read(lpRequestId, RequestId.class);
			LOG.info("WFSAsyncRegister: errorCode={},requestId={},eventClasses={},hWndReg={},hWnd={}", errorCode,
					requestId, eventClasses, hWndReg, hWnd);
			if (errorCode != 0) {
				throw XfsExceptionFactory.create(errorCode);
			}
		} finally {
			memorySystem.free(hService);
			memorySystem.free(dwEventClass);
			memorySystem.free(lpRequestId);
		}
		return requestId;
	}

	@Override
	public RequestId asyncUnlock(ServiceId serviceId, Address hWnd) throws XfsException {
		RequestId result;
		Address hService = memorySystem.write(serviceId);
		Address lpRequestId = memorySystem.write(memorySystem.nullValue());
		try {
			int errorCode = wfsAsyncUnlock(hService.getValue(), hWnd.getValue(), lpRequestId.getValue());
			if (errorCode != 0) {
				LOG.info("WFSAsyncUnlock: errorCode={},serviceId={},hWnd={}", errorCode, serviceId, hWnd);
				throw XfsExceptionFactory.create(errorCode);
			}
			result = memorySystem.read(lpRequestId, RequestId.class);
		} finally {
			memorySystem.free(hService);
			memorySystem.free(lpRequestId);
		}
		return result;
	}

	@Override
	public void cancelAsyncRequest(ServiceId serviceId, RequestId aRequestId) throws XfsException {
		Address hService = memorySystem.write(serviceId);
		Address requestId = memorySystem.write(aRequestId);
		try {
			LOG.debug("hService={},requestId={}", hService, requestId);
			int errorCode = wfsCancelAsyncRequest(hService.getValue(), requestId.getValue());
			LOG.info("WFSCancelAsyncRequest: serviceId={}, requestId={}, errorCode={}", serviceId, aRequestId,
					errorCode);
			if (errorCode != 0) {
				throw XfsExceptionFactory.create(errorCode);
			}
		} finally {
			memorySystem.free(hService);
			memorySystem.free(requestId);
		}
	}

	@Override
	public void cleanUp() throws XfsException {
		int errorCode = wfsCleanUp();
		LOG.info("WFSCleanUp: errorCode={}", errorCode);
		if (errorCode != 0) {
			throw XfsExceptionFactory.create(errorCode);
		}
	}

	@Override
	public Address createAppHandle() throws XfsException {
		Address lphApp = memorySystem.write(memorySystem.nullValue());
		int errorCode = wfsCreateAppHandle(lphApp.getValue());
		Address result = memorySystem.read(lphApp, Address.class);
		memorySystem.free(lphApp);
		if (errorCode != 0) {
			LOG.info("WFSCreateAppHandle: errorCode={}", errorCode);
			throw XfsExceptionFactory.create(errorCode);
		}
		LOG.info("WFSCreateAppHandle: result={},lphApp={}", result, lphApp);
		return result;
	}

	@Override
	public void destroyAppHandle(Address hApp) throws XfsException {
		LOG.debug("WFSDestroyAppHandle: hApp={}", hApp);
		int errorCode = wfsDestroyAppHandle(hApp.getValue());
		LOG.info("WFSDestroyAppHandle: errorCode={},hApp={}", errorCode, hApp);
		if (errorCode != 0) {
			throw XfsExceptionFactory.create(errorCode);
		}
	}

	@Override
	public void freeResult(Address address) throws XfsException {
		int errorCode = wfsFreeResult(address.getValue());
		LOG.debug("WFSFreeResult: errorCode={},address={}", errorCode, address);
		if (errorCode != 0) {
			throw XfsExceptionFactory.create(errorCode);
		}
	}

	@Override
	public MemorySystem getMemorySystem() {
		return memorySystem;
	}

	@Override
	public <E extends Enum<E> & XfsConstant> void setTraceLevel(ServiceId serviceId, Set<E> traceLevel)
			throws XfsException {
		Address hService = memorySystem.write(serviceId);
		Address dwTraceLevel = memorySystem.write(XfsEnumSet32Wrapper.build(traceLevel));
		try {
			int errorCode = wfmSetTraceLevel(hService.getValue(), dwTraceLevel.getValue());
			LOG.info("WFMSetTraceLevel: errorCode={},serviceId={},traceLevel={}", errorCode, serviceId, traceLevel);
			if (errorCode != 0) {
				throw XfsExceptionFactory.create(errorCode);
			}
		} finally {
			memorySystem.free(hService);
			memorySystem.free(dwTraceLevel);
		}
	}

	@Override
	public WfsVersion startUp(XfsVersion lowVersion, XfsVersion highVersion) throws StartUpException {
		Address versionsRequired = memorySystem.write(VersionsRequired.build(lowVersion, highVersion));
		Address versionAddress = memorySystem.write(new WfsVersion.Builder().build());
		LOG.debug("versionsRequired={},versionAddress={}", versionsRequired, versionAddress);
		int errorCode = wfsStartUp(versionsRequired.getValue(), versionAddress.getValue());
		WfsVersion wfsVersion = memorySystem.read(versionAddress, WfsVersion.class);
		LOG.info("WFSStartUp: errorCode={},lowVersion={},highVersion={},wfsVersion={}", errorCode, lowVersion,
				highVersion, wfsVersion);
		memorySystem.free(versionsRequired);
		memorySystem.free(versionAddress);
		if (errorCode != 0) {
			throw StartUpException.build(errorCode, wfsVersion);
		}
		return wfsVersion;
	}
}
