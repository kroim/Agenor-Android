package global;

import com.zerocoinj.core.CoinDenomination;
import com.zerocoinj.core.ZCoin;

import org.pivxj.core.Address;
import org.pivxj.core.Coin;
import org.pivxj.core.Context;
import org.pivxj.core.InsufficientMoneyException;
import org.pivxj.core.Peer;
import org.pivxj.core.PeerGroup;
import org.pivxj.core.Sha256Hash;
import org.pivxj.core.Transaction;
import org.pivxj.core.TransactionInput;
import org.pivxj.core.TransactionOutput;
import org.pivxj.crypto.DeterministicKey;
import org.pivxj.crypto.MnemonicException;
import org.pivxj.wallet.DeterministicKeyChain;
import org.pivxj.wallet.SendRequest;
import org.pivxj.wallet.exceptions.RequestFailedErrorcodeException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import global.exceptions.UpgradeException;
import global.wrappers.InputWrapper;
import global.wrappers.TransactionWrapper;
import global.exceptions.CantSweepBalanceException;
import global.exceptions.ContactAlreadyExistException;
import global.exceptions.NoPeerConnectedException;
import host.furszy.zerocoinj.wallet.AmountPerDen;
import host.furszy.zerocoinj.wallet.CannotSpendCoinsException;
import host.furszy.zerocoinj.wallet.MultiWallet;
import wallet.exceptions.InsufficientInputsException;
import wallet.exceptions.TxNotFoundException;
import wallet.exceptions.CantRestoreEncryptedWallet;

/**
 * Created by mati on 18/04/17.
 */

public interface AgenorModule {

    /**
     * Initialize the module
     */
    void start() throws IOException;

    /**
     * ...
     */
    void createWallet();

    boolean backupWallet(File backupFile, String password) throws IOException;

    /**
     *
     *
     * @param backupFile
     */
    void restoreWallet(File backupFile) throws IOException;

    /**
     *
     * @param file
     * @param password
     * @param forceRestoreVersion force the restauration from a previous wallet version
     * @throws CantRestoreEncryptedWallet
     * @throws IOException
     */
    void restoreWalletFromEncrypted(File file, String password, int forceRestoreVersion) throws CantRestoreEncryptedWallet, IOException;

    void restoreWallet(List<String> mnemonic, long timestamp,boolean bip44) throws IOException, MnemonicException;

    /**
     * If the wallet already exist
     * @return
     */
    boolean isWalletCreated();

    /**
     * Return a new address.
     */
    Address getReceiveAddress();

    Address getFreshNewAddress();

    boolean isAddressUsed(Address address);

    long getAvailableBalance();

    Coin getAvailableBalanceCoin();
    Coin getUnnavailableBalanceCoin();

    Coin getZpivAvailableBalanceCoin();
    Coin getZpivUnnavailableBalanceCoin();

    boolean isWalletWatchOnly();

    BigDecimal getAvailableBalanceLocale();

    /******    Address Label          ******/

    List<AddressLabel> getContacts();

    AddressLabel getAddressLabel(String address);

    List<AddressLabel> getMyAddresses();

    void saveContact(AddressLabel addressLabel) throws ContactAlreadyExistException;

    void saveContactIfNotExist(AddressLabel addressLabel);

    void deleteAddressLabel(AddressLabel data);


    /******   End Address Label          ******/


    boolean chechAddress(String addressBase58);

    Transaction buildSendTx(String addressBase58, Coin amount, String memo,Address changeAddress) throws InsufficientMoneyException;
    Transaction buildSendTx(String addressBase58, Coin amount,Coin feePerKb, String memo,Address changeAddress) throws InsufficientMoneyException;

    WalletConfiguration getConf();

    List<TransactionWrapper> listTx();
    List<TransactionWrapper> listPrivateTxes();

    Collection<Transaction> listPendingTxes();

    Coin getValueSentFromMe(Transaction transaction, boolean excludeChangeAddress);
    //
    Coin getZpivValueSentToMe(Transaction transaction);

    void commitTx(Transaction transaction);

    List<Peer> listConnectedPeers();

    int getChainHeight();

    AgenorRate getRate(String selectedRateCoin);

    List<InputWrapper> listUnspentWrappers();
    List<TransactionOutput> listZpivUnspents();

    Set<InputWrapper> convertFrom(List<TransactionInput> list) throws TxNotFoundException;

    Transaction getTx(Sha256Hash txId);

    List<String> getMnemonic();

    String getWatchingPubKey();
    DeterministicKey getWatchingKey();

    DeterministicKey getKeyPairForAddress(Address address);

    TransactionOutput getUnspent(Sha256Hash parentTxHash, int index) throws TxNotFoundException;

    List<TransactionOutput> getRandomUnspentNotInListToFullCoins(List<TransactionInput> inputs, Coin amount) throws InsufficientInputsException;

    Transaction completeTx(Transaction transaction,Address changeAddress,Coin fee) throws InsufficientMoneyException;
    Transaction completeTx(Transaction transaction) throws InsufficientMoneyException;

    Transaction completeTxWithCustomFee(Transaction transaction,Coin fee) throws InsufficientMoneyException;

    Coin getUnspentValue(Sha256Hash parentTransactionHash, int index);

    boolean isAnyPeerConnected();

    long getConnectedPeerHeight();

    int getProtocolVersion();

    void checkMnemonic(List<String> mnemonic) throws MnemonicException;

    boolean isSyncWithNode() throws NoPeerConnectedException;

    void watchOnlyMode(String xpub, DeterministicKeyChain.KeyChainType keyChainType) throws IOException;

    boolean isBip32Wallet();

    boolean sweepBalanceToNewSchema() throws InsufficientMoneyException, CantSweepBalanceException;

    boolean upgradeWallet(String upgradeCode) throws UpgradeException;

    List<AgenorRate> listRates();

    List<String> getAvailableMnemonicWordsList();

    /**
     * Encrypt the wallet
     * @param password
     * @return
     */
    boolean encrypt(String password) throws UnsupportedEncodingException;
    boolean decrypt(String password) throws UnsupportedEncodingException;
    boolean isWalletLocked();

    //
    SendRequest createMint(Coin value) throws InsufficientMoneyException;
    SendRequest createSpend(Address to, Coin amount, boolean mintChange) throws InsufficientMoneyException;
    Transaction spendZpiv(Context context, SendRequest sendRequest, PeerGroup peerGroup, ExecutorService executor) throws InsufficientMoneyException, CannotSpendCoinsException, RequestFailedErrorcodeException;

    ZCoin getAssociatedCoin(BigInteger commitmentValue);

    boolean isEveryOutputSpent(Transaction transaction, MultiWallet.WalletType type);

    List<AmountPerDen> listAmountPerDen();

    Map<CoinDenomination, HashSet<ZCoin>> getAllMintedZCoins();

    boolean isStarted();

    boolean isStarting();
}
