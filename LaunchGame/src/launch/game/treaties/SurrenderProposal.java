/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.treaties;

import java.nio.ByteBuffer;

/**
 *
 * @author tobster
 */
public class SurrenderProposal extends Treaty
{
    public SurrenderProposal(int lID, int lAllianceID1, int lAllianceID2)
    {
        super(lID, lAllianceID1, lAllianceID2);
    }
    
    public SurrenderProposal(ByteBuffer bb)
    {
        super(bb);
    }

    @Override
    public Type GetType()
    {
        return Type.SURRENDER_PROPOSAL;
    }
}
