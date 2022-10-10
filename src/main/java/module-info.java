module com.yasuenag.hwrand {
  requires com.yasuenag.ffmasm;
  exports com.yasuenag.hwrand.x86;
  provides java.security.Provider with com.yasuenag.hwrand.x86.HWRandX86Provider;
}
